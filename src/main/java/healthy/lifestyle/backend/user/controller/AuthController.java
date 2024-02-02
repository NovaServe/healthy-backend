package healthy.lifestyle.backend.user.controller;

import healthy.lifestyle.backend.user.dto.*;
import healthy.lifestyle.backend.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.basePath}/${api.version}/users/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/validate")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<?> validateToken() {
        return ResponseEntity.ok().build();
    }
}
