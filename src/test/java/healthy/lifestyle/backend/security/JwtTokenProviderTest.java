package healthy.lifestyle.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * @see JwtTokenProvider
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    @Mock
    SecurityProps securityProps;

    @Mock
    UserServiceImpl userService;

    @InjectMocks
    JwtTokenProvider jwtTokenProvider;

    @Test
    void generateToken() {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String actual = jwtTokenProvider.generateToken(authentication);
        assertNotNull(actual);
        assertTrue(actual.length() > 0);
        String[] tokenParts = actual.split("\\.");
        assertEquals(3, tokenParts.length);
        verify(securityProps, times(2)).Jwt();
    }

    @Test
    void getUsernameFromJwt() {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        String actual = jwtTokenProvider.getUsernameFromJwt(token);
        assertEquals(usernameOrEmail, actual);
    }

    @Test
    void validateToken() {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Role role = new Role.Builder().id(1L).name("ROLE_USER").build();
        User user = new User.Builder()
                .id(1L)
                .username(usernameOrEmail)
                .email(usernameOrEmail)
                .fullName("Test Full Name")
                .password(password)
                .role(role)
                .build();
        jwtTokenProvider.setUserService(userService);
        when(userService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail))
                .thenReturn(Optional.of(user));
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        boolean isValidated = jwtTokenProvider.validateToken(token);
        assertTrue(isValidated);
        verify(userService, times(1)).findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    @Test
    void validateToken_UserNotFound() throws InterruptedException {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        jwtTokenProvider.setUserService(userService);
        when(userService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail))
                .thenReturn(Optional.empty());
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        ApiException exception = assertThrows(ApiException.class, () -> jwtTokenProvider.validateToken(token));
        assertEquals(ErrorMessage.INVALID_TOKEN.getName(), exception.getMessage());
    }

    @Test
    void validateToken_Expired() throws InterruptedException {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                1L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        Thread.sleep(2L);
        ApiException exception = assertThrows(ApiException.class, () -> jwtTokenProvider.validateToken(token));
        assertEquals(ErrorMessage.INVALID_TOKEN.getName(), exception.getMessage());
    }

    @Test
    void validateToken_InvalidHeader() {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        String[] tokenParts = token.split("\\.");
        StringBuilder reversed = new StringBuilder(tokenParts[0]).reverse();
        String reversedString = reversed.toString();
        String invalidToken = reversedString + "." + tokenParts[1] + "." + tokenParts[2];
        ApiException exception = assertThrows(ApiException.class, () -> jwtTokenProvider.validateToken(invalidToken));
        assertEquals(ErrorMessage.INVALID_TOKEN.getName(), exception.getMessage());
    }

    @Test
    void validateToken_InvalidPayload() {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        String[] tokenParts = token.split("\\.");
        StringBuilder reversed = new StringBuilder(tokenParts[1]).reverse();
        String reversedString = reversed.toString();
        String invalidToken = tokenParts[0] + "." + reversedString + "." + tokenParts[2];
        ApiException exception = assertThrows(ApiException.class, () -> jwtTokenProvider.validateToken(invalidToken));
        assertEquals(ErrorMessage.INVALID_TOKEN.getName(), exception.getMessage());
    }

    @Test
    void validateToken_InvalidSignature() {
        SecurityProps.Jwt jwt = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        String[] tokenParts = token.split("\\.");
        StringBuilder reversed = new StringBuilder(tokenParts[1]).reverse();
        String reversedString = reversed.toString();
        String invalidToken = tokenParts[0] + "." + tokenParts[1] + "." + reversedString;
        ApiException exception = assertThrows(ApiException.class, () -> jwtTokenProvider.validateToken(invalidToken));
        assertEquals(ErrorMessage.INVALID_TOKEN.getName(), exception.getMessage());
    }

    @Test
    void validateToken_InvalidTimeDelta() {
        SecurityProps.Jwt jwt1 = new SecurityProps.Jwt(
                60000L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        SecurityProps.Jwt jwt2 = new SecurityProps.Jwt(
                60001L,
                "test4qa005b6fe1eab42822419e609765bbd1bb60875dadf1ea9d19016ee50cc0236ec6f0dac8fb244f15dceb02d71584629330b4f9dac5a689619e9b71b8fc2");
        when(securityProps.Jwt()).thenReturn(jwt1).thenReturn(jwt2);
        String usernameOrEmail = "test@email.com";
        String password = "test-password";
        Authentication authentication = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
        String token = jwtTokenProvider.generateToken(authentication);
        ApiException exception = assertThrows(ApiException.class, () -> jwtTokenProvider.validateToken(token));
        assertEquals(ErrorMessage.INVALID_TOKEN.getName(), exception.getMessage());
    }
}
