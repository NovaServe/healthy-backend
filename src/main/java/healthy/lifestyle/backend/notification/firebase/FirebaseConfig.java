package healthy.lifestyle.backend.notification.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {
    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        //        GoogleCredentials googleCredentials = GoogleCredentials
        //                .fromStream(new FileInputStream("service-account.json"));
        //        FirebaseOptions options = FirebaseOptions.builder()
        //                .setCredentials(googleCredentials)
        //                .build();

        FileInputStream serviceAccount = new FileInputStream("firebase-service-account.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        return FirebaseMessaging.getInstance(app);
    }
}
