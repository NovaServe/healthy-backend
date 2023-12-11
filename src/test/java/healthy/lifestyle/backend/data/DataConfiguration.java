package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.data.bodypart.BodyPartJpaTestBuilder;
import healthy.lifestyle.backend.data.country.CountryJpaTestBuilder;
import healthy.lifestyle.backend.data.exercise.ExerciseDtoTestBuilder;
import healthy.lifestyle.backend.data.exercise.ExerciseJpaTestBuilder;
import healthy.lifestyle.backend.data.httpref.HttpRefJpaTestBuilder;
import healthy.lifestyle.backend.data.user.UserDtoTestBuilder;
import healthy.lifestyle.backend.data.user.UserJpaTestBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class DataConfiguration {
    @Bean
    public DataHelper dataHelper() {
        return new DataHelper();
    }

    @Bean
    public DataUtil dataUtil() {
        return new DataUtil();
    }

    @Bean
    public BodyPartJpaTestBuilder bodyPartJpaTestBuilder() {
        return new BodyPartJpaTestBuilder();
    }

    @Bean
    public HttpRefJpaTestBuilder mediaJpaTestBuilder() {
        return new HttpRefJpaTestBuilder();
    }

    @Bean
    public ExerciseJpaTestBuilder exerciseJpaTestBuilder() {
        return new ExerciseJpaTestBuilder();
    }

    @Bean
    public UserJpaTestBuilder userJpaTestBuilder() {
        return new UserJpaTestBuilder();
    }

    @Bean
    public CountryJpaTestBuilder countryJpaTestBuilder() {
        return new CountryJpaTestBuilder();
    }

    @Bean
    public ExerciseDtoTestBuilder exerciseDtoTestBuilder() {
        return new ExerciseDtoTestBuilder();
    }

    @Bean
    public UserDtoTestBuilder userDtoTestBuilder() {
        return new UserDtoTestBuilder();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
