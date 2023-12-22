package healthy.lifestyle.backend.users.controller;

import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.users.service.CountryService;
import healthy.lifestyle.backend.users.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("${api.basePath}/${api.version}/users")
public class UserController {
    private final CountryService countryService;

    private final UserService userService;

    private final AuthService authService;

    public UserController(CountryService countryService, UserService userService, AuthService authService) {
        this.countryService = countryService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserResponseDto> getUserDetails() {
        Long authUserId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(userService.getUserDetailsById(authUserId));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("userId") @NotNull @PositiveOrZero Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {
        authService.checkAuthUserIdAndParamUserId(
                SecurityContextHolder.getContext().getAuthentication(), userId);
        return ResponseEntity.ok(userService.updateUser(userId, requestDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") @NotNull @PositiveOrZero Long userId) {
        authService.checkAuthUserIdAndParamUserId(
                SecurityContextHolder.getContext().getAuthentication(), userId);
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDto>> getCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }
}
