package healthy.lifestyle.backend.notification.firebase;

import healthy.lifestyle.backend.user.service.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${api.basePath}/${api.version}/calendar/notification")
public class FirebaseController {
    @Autowired
    AuthUtil authUtil;

    @Autowired
    FirebaseService firebaseService;

    @PostMapping("/handleFirebaseUserToken")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> handleFirebaseUserToken(
            HttpServletRequest request, @RequestBody FirebaseRequestDto requestDto) {
        if (requestDto.getFirebaseUserTokenFromLocalStorage() == null
                && requestDto.getFirebaseUserTokenNowReceived() == null) {
            return ResponseEntity.badRequest().build();
        } else {
            long userId = authUtil.getUserIdFromAuthentication(
                    SecurityContextHolder.getContext().getAuthentication());
            String userAgent = request.getHeader("User-Agent");
            firebaseService.handleFirebaseUserToken(userId, requestDto, userAgent);
            return ResponseEntity.ok().build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirebaseRequestDto {
        private String firebaseUserTokenFromLocalStorage;

        private String firebaseUserTokenNowReceived;
    }
}
