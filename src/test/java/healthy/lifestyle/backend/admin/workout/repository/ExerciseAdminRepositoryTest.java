package healthy.lifestyle.backend.admin.workout.repository;

import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Import(BeanConfig.class)
class ExerciseAdminRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    ExerciseAdminRepository exerciseAdminRepository;

    @Autowired
    DbUtil dbUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("multipleFilters")
    void findByFiltersTest_shouldReturnListOfExercises(
            String title, String description, Boolean isCustom, Boolean needsEquipment, List<Integer> resultSeeds) {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef httpRef1 = dbUtil.createDefaultHttpRef(1);
        Exercise exercise1 = dbUtil.createDefaultExercise(1, true, List.of(bodyPart1), List.of(httpRef1));

        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        Exercise exercise2 = dbUtil.createDefaultExercise(2, true, List.of(bodyPart1), List.of(httpRef1));

        User user = dbUtil.createUser(1);
        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(3, true, List.of(bodyPart3), List.of(customHttpRef1), user);

        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(4, true, List.of(bodyPart4), List.of(customHttpRef2), user);

        // When
        Optional<List<Exercise>> resultOptional =
                exerciseAdminRepository.findByFilters(title, description, isCustom, needsEquipment);

        // Then
        assertTrue(resultOptional.isPresent());
        List<Exercise> result = resultOptional.get();

        // assertEquals(resultSeeds.size(), result.size());
        for (int i = 0; i < resultSeeds.size(); i++) {
            assertEquals("Exercise " + resultSeeds.get(i), result.get(i).getTitle());
            assertEquals("Desc " + resultSeeds.get(i), result.get(i).getDescription());
        }
    }

    static Stream<Arguments> multipleFilters() {
        return Stream.of(
                // Positive cases for default exercises
                Arguments.of(null, null, false, false, List.of(1, 2)),
                Arguments.of(null, null, false, false, List.of(1, 2)),
                Arguments.of("Exercise 1", "Desc 1", false, false, List.of(1)),
                Arguments.of("Exercise 2", "Desc 2", false, false, List.of(2)),

                // Negative cases for default exercises
                Arguments.of(null, null, true, false, Collections.emptyList()),
                Arguments.of(null, null, true, true, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, false, false, Collections.emptyList()),
                Arguments.of(null, "NonExistentValue", false, false, Collections.emptyList()),

                // Positive cases for custom exercises
                Arguments.of(null, null, true, false, List.of(3, 4)),
                Arguments.of(null, null, true, true, List.of(3, 4)),
                Arguments.of("Exercise 3", "Desc 3", true, true, List.of(3)),

                // Negative cases for custom exercises
                Arguments.of("NonExistentValue", null, true, true, Collections.emptyList()),
                Arguments.of(null, "NonExistentValue", true, true, Collections.emptyList()),
                Arguments.of("NonExistentValue", "NonExistentValue", true, true, Collections.emptyList()));
    }
}
