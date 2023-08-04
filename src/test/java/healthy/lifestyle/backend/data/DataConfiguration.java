package healthy.lifestyle.backend.data;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DataConfiguration {
    @Bean
    public DataHelper dataHelper() {
        return new DataHelper();
    }
}
