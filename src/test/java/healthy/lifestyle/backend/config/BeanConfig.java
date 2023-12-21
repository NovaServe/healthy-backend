package healthy.lifestyle.backend.config;

import healthy.lifestyle.backend.util.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class BeanConfig {
    @Bean
    public DbUtil jpaUtil() {
        return new DbUtil();
    }

    @Bean
    public TestUtil dataUtil() {
        return new TestUtil();
    }

    @Bean
    public DtoUtil dtoUtil() {
        return new DtoUtil();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
