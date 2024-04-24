package healthy.lifestyle.backend.user.controller;

import healthy.lifestyle.backend.user.dto.*;
import healthy.lifestyle.backend.user.service.AuthService;
import healthy.lifestyle.backend.user.service.AuthUtil;
import healthy.lifestyle.backend.user.service.UserService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.basePath}/${api.version}/users/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    UserService userService;

    @Value("${firebase.vapid-key}")
    String firebaseVapidKey;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/validate")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<?> validateToken() {
        long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        UserResponseDto user = userService.getUserDetailsById(userId);
        Map<Integer, String> response = new HashMap<>();
        response.put(1, user.getFullName());
        response.put(2, firebaseVapidKey);
        return ResponseEntity.ok(response);
    }
}
