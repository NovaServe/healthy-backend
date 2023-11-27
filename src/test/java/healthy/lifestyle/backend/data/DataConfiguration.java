package healthy.lifestyle.backend.data;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

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
    public ExerciseJpaTestBuilder exerciseJpaTestBuilder() {
        return new ExerciseJpaTestBuilder();
    }

    @Bean
    public UserJpaTestBuilder userJpaTestBuilder() {
        return new UserJpaTestBuilder();
    }

    @Bean
    public ExerciseDtoTestBuilder exerciseDtoTestBuilder() {
        return new ExerciseDtoTestBuilder();
    }
}
