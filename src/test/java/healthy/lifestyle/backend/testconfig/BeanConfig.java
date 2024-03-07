package healthy.lifestyle.backend.testconfig;

import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.testutil.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class BeanConfig {
    @Bean
    public DbUtil dbUtil() {
        return new DbUtil();
    }

    @Bean
    public DtoUtil dtoUtil() {
        return new DtoUtil();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return Mockito.mock(FirebaseMessaging.class);
    }
}
