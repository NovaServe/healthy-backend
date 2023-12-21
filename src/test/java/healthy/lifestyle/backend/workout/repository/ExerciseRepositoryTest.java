package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Import(BeanConfig.class)
class ExerciseRepositoryTest {
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
    ExerciseRepository exerciseRepository;

    @Autowired
    DbUtil dbUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    void findAllDefault_shouldReturnDefaultExerciseList() {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));

        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(2, needsEquipment, List.of(bodyPart2), List.of(defaultHttpRef2));

        User user = dbUtil.createUser(1);
        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef1), user);

        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(4, needsEquipment, List.of(bodyPart4), List.of(customHttpRef2), user);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<Exercise> exercisesActual = exerciseRepository.findAllDefault(sort);

        // Then
        assertEquals(2, exercisesActual.size());
        assertThat(exercisesActual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "bodyParts", "users")
                .isEqualTo(List.of(defaultExercise1, defaultExercise2));
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnCustomExercise() {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));

        User user = dbUtil.createUser(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);

        // When
        Optional<Exercise> actualExerciseOpt =
                exerciseRepository.findCustomByTitleAndUserId(customExercise1.getTitle(), user.getId());

        // Then
        assertTrue(actualExerciseOpt.isPresent());
        Exercise actualExercise = actualExerciseOpt.get();
        assertEquals(customExercise1.getId(), actualExercise.getId());
    }

    @Test
    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenWrongTitleGiven() {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));

        User user = dbUtil.createUser(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);
        String nonExistentTitle = customExercise1.getTitle() + " non existent title";

        // When
        Optional<Exercise> actualOpt = exerciseRepository.findCustomByTitleAndUserId(nonExistentTitle, user.getId());

        // Then
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenWrongUserIdGiven() {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));

        User user = dbUtil.createUser(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);

        long nonExistentUserId = 1000L;

        // When
        Optional<Exercise> actualOpt =
                exerciseRepository.findCustomByTitleAndUserId(customExercise1.getTitle(), nonExistentUserId);

        // Then
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenDefaultExerciseTitleGiven() {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));

        User user = dbUtil.createUser(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);

        // When
        Optional<Exercise> actualOpt =
                exerciseRepository.findCustomByTitleAndUserId(defaultExercise.getTitle(), user.getId());

        // Then
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findCustomByUserIdTest_shouldReturnCustomExerciseList_whenUserIdGiven() {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));

        User user = dbUtil.createUser(1, role, country);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);

        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise3 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef3), user2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<Exercise> exercisesActual = exerciseRepository.findCustomByUserId(user.getId(), sort);

        // Then
        assertEquals(2, exercisesActual.size());
        assertThat(exercisesActual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "bodyParts", "user")
                .isEqualTo(List.of(customExercise1, customExercise2));
    }

    @Test
    void findCustomByExerciseIdAndUserIdTest_shouldReturnCustomExercise() {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));

        User user = dbUtil.createUser(1, role, country);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Exercise customExercise1 =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise2 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);

        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(3, user);
        Exercise customExercise3 =
                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef3), user2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        Optional<Exercise> exercisesOptional =
                exerciseRepository.findCustomByExerciseIdAndUserId(customExercise1.getId(), user.getId());

        // Then
        assertTrue(exercisesOptional.isPresent());
        Exercise exerciseActual = exercisesOptional.get();
        assertThat(exerciseActual)
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "bodyParts", "user")
                .isEqualTo(customExercise1);
    }

    @Test
    void findCustomByExerciseIdAndUserIdTest_shouldReturnOptionalEmpty_whenInvalidIdGiven() {
        long nonExistingExerciseId = 1000L;
        long nonExistingUserId = 1000L;

        // When
        Optional<Exercise> exerciseOptional =
                exerciseRepository.findCustomByExerciseIdAndUserId(nonExistingExerciseId, nonExistingUserId);

        // Then
        assertTrue(exerciseOptional.isEmpty());
    }

    @Test
    void findCustomByExerciseIdAndUserIdTest_shouldReturnOptionalEmpty_whenUserExerciseMismatch() {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        Exercise customExercise1 = dbUtil.createCustomExercise(
                1, true, List.of(bodyPart1), List.of(defaultHttpRef1, customHttpRef1), user1);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);
        Exercise customExercise2 = dbUtil.createCustomExercise(
                2, true, List.of(bodyPart2), List.of(defaultHttpRef2, customHttpRef2), user1);

        // When
        Optional<Exercise> exerciseOptional =
                exerciseRepository.findCustomByExerciseIdAndUserId(customExercise1.getId(), user2.getId());

        // Then
        assertTrue(exerciseOptional.isEmpty());
    }
}
