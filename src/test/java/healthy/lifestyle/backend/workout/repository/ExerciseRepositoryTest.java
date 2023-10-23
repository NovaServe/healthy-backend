package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Exercise;
import java.util.*;
import java.util.stream.IntStream;
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
@Import(DataConfiguration.class)
class ExerciseRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    DataHelper dataHelper;

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnCustomExercise() {
        // Given
        Exercise exercise = dataHelper.createExercise(1, true, false, null, null);
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        Exercise otherExercise = dataHelper.createExercise(2, true, false, null, null);
        User otherUser = dataHelper.createUser("two", role, country, Set.of(otherExercise));
        dataHelper.exerciseAddUsers(otherExercise, Set.of(otherUser));

        // When
        Optional<Exercise> actualExerciseOpt =
                exerciseRepository.findCustomByTitleAndUserId(exercise.getTitle(), user.getId());

        // Then
        assertTrue(actualExerciseOpt.isPresent());
        Exercise actualExercise = actualExerciseOpt.get();
        assertEquals(exercise.getTitle(), actualExercise.getTitle());
        assertEquals(exercise.getDescription(), actualExercise.getDescription());
        assertEquals(exercise.isCustom(), actualExercise.isCustom());
    }

    @Test
    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenWrongTitleProvided() {
        // Given
        Exercise exercise = dataHelper.createExercise(1, true, false, null, null);
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        // When
        Optional<Exercise> actualOpt = exerciseRepository.findCustomByTitleAndUserId("Wrong title", user.getId());

        // Then
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenWrongUserIdProvided() {
        // Given
        Exercise exercise = dataHelper.createExercise(1, true, false, null, null);
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));
        long wrongUserId = 9999L;

        // When
        Optional<Exercise> actualOpt = exerciseRepository.findCustomByTitleAndUserId(exercise.getTitle(), wrongUserId);

        // Then
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenDefaultExerciseTitleProvided() {
        // Given
        Exercise exercise = dataHelper.createExercise(1, false, false, null, null);
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        // When
        Optional<Exercise> actualOpt = exerciseRepository.findCustomByTitleAndUserId(exercise.getTitle(), user.getId());

        // Then
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findAllDefault() {
        // Given
        List<Exercise> exercisesDefault = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createExercise(id, false, false, null, null))
                .toList();

        List<Exercise> exercisesCustom = IntStream.rangeClosed(3, 4)
                .mapToObj(id -> dataHelper.createExercise(id, true, false, null, null))
                .toList();

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<Exercise> exercisesActual = exerciseRepository.findAllDefault(sort);

        // Then
        assertEquals(exercisesDefault.size(), exercisesActual.size());
        assertThat(exercisesDefault)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "bodyParts", "users")
                .isEqualTo(exercisesActual);
    }

    @Test
    void findCustomByUserIdTest_shouldReturnCustomExercises_whenUserIdProvided() {
        // Given
        List<Exercise> customExercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createExercise(id, true, false, null, null))
                .toList();

        List<Exercise> defaultExercises = IntStream.rangeClosed(3, 4)
                .mapToObj(id -> dataHelper.createExercise(id, false, false, null, null))
                .toList();

        Exercise otherExercise = dataHelper.createExercise(2, true, false, null, null);

        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, new HashSet<>(customExercises));
        User otherUser = dataHelper.createUser("two", role, country, Set.of(otherExercise));

        dataHelper.exerciseAddUsers(customExercises.get(0), Set.of(user));
        dataHelper.exerciseAddUsers(customExercises.get(1), Set.of(user));
        dataHelper.exerciseAddUsers(otherExercise, Set.of(otherUser));

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<Exercise> exercisesActual = exerciseRepository.findCustomByUserId(user.getId(), sort);

        // Then
        assertEquals(customExercises.size(), exercisesActual.size());
        assertThat(customExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "bodyParts", "users")
                .isEqualTo(exercisesActual);
    }
}
