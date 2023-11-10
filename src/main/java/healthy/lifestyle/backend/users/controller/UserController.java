package healthy.lifestyle.backend.users.controller;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.common.AuthUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import healthy.lifestyle.backend.users.dto.UpdateUserRequestDto;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.service.CountryService;
import healthy.lifestyle.backend.users.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.basePath}/${api.version}/users")
public class UserController {
    private final CountryService countryService;
    private final AuthUtil authUtil;
    private final UserService userService;

    public UserController(CountryService countryService, AuthUtil authUtil, UserService userService) {
        this.countryService = countryService;
        this.authUtil = authUtil;
        this.userService = userService;
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDto>> getCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("userId") Long userId, @Valid @RequestBody UpdateUserRequestDto requestDto) {
        Long authenticatedUserId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        if (isNull(authenticatedUserId) || !authenticatedUserId.equals(userId))
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(userService.updateUser(userId, requestDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Long> deleteUser(@PathVariable("userId") Long userId) {
        Long authenticatedUserId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        if (isNull(authenticatedUserId) || !authenticatedUserId.equals(userId))
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(userService.deleteUser(userId), HttpStatus.NO_CONTENT);
    }
}
