package healthy.lifestyle.backend.user.controller;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.user.dto.*;
import healthy.lifestyle.backend.user.service.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    CountryService countryService;

    @Autowired
    TimezoneService timezoneService;

    @Autowired
    UserService userService;

    @Autowired
    AuthUtil authUtil;

    @PostMapping()
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserResponseDto> getUserDetails() {
        Long authUserId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        UserResponseDto responseDto = userService.getUserDetailsById(authUserId);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("userId") @IdValidation long userId, @Valid @RequestBody UserUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {
        authUtil.checkAuthUserIdAndParamUserId(
                SecurityContextHolder.getContext().getAuthentication(), userId);
        UserResponseDto responseDto = userService.updateUser(userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") @IdValidation long userId) {
        authUtil.checkAuthUserIdAndParamUserId(
                SecurityContextHolder.getContext().getAuthentication(), userId);
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDto>> getCountries() {
        List<CountryResponseDto> responseDtoList = countryService.getCountries();
        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping("/timezones")
    public ResponseEntity<List<TimezoneResponseDto>> getTimezones(){
        List<TimezoneResponseDto> responseDtoList = timezoneService.getTimezones();
        return ResponseEntity.ok(responseDtoList);
    }
}
