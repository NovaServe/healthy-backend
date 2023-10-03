package healthy.lifestyle.backend.users.controller;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.LoginResponseDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;
import healthy.lifestyle.backend.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}/${api.version}/users/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates new user
     * @throws healthy.lifestyle.backend.exception.GlobalExceptionHandler Validation error
     * @throws ApiException (ErrorMessage.ALREADY_EXISTS, HttpStatus.BAD_REQUEST)<br>
     * If user already exists
     * @throws ApiException (ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)<br>
     * If ROLE_USER is not found
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        SignupResponseDto responseDto = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Logins user
     * @throws healthy.lifestyle.backend.exception.GlobalExceptionHandler Validation error
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = userService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
