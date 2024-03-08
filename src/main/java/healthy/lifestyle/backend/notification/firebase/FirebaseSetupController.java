package healthy.lifestyle.backend.notification.firebase;

import java.util.HashMap;
import java.util.Map;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${api.basePath}/${api.version}/calendar/notification")
public class FirebaseSetupController {

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

    @PostMapping("/setFirebaseClientToken")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> setFirebaseClientToken(@RequestBody FirebaseRequestDto requestDto) {
        // Setup token with timestamp in database
        // https://firebase.google.com/docs/cloud-messaging/manage-tokens

        Map<String, String> data = new HashMap<>();
        data.put("dataKey1", "dataValue1");
        data.put("dataKey2", "dataValue2");

        FirebaseMessageDto firebaseMessageDto = FirebaseMessageDto.builder()
                .firebaseUserToken(requestDto.getFirebaseClientToken())
                .title("15 min")
                .body("Workout starts in 15 min")
                .build();

        firebaseMessagingService.sendMessage(firebaseMessageDto);

        return ResponseEntity.ok().build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirebaseRequestDto {
        private String firebaseClientToken;
    }
}
