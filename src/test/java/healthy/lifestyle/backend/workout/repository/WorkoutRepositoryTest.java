package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
@Import(DataConfiguration.class)
class WorkoutRepositoryTest {
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
    WorkoutRepository workoutRepository;

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
    void findCustomByTitleAndUserIdTest_shouldReturnCustomWorkout() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);
        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user1, Set.of(workout1, workout2));

        User user2 = dataHelper.createUser("two", role, country, null, 20);
        Workout workout3 = dataHelper.createWorkout(3, true, null);
        dataHelper.userAddWorkout(user2, Set.of(workout3));

        Workout workout4 = dataHelper.createWorkout(4, false, null);

        // When
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(workout1.getTitle(), user1.getId());

        // Then
        assertEquals(1, workouts.size());
        assertEquals(workout1.getId(), workouts.get(0).getId());
        assertEquals(workout1.getTitle(), workouts.get(0).getTitle());
        assertEquals(workout1.getDescription(), workouts.get(0).getDescription());
        assertEquals(workout1.isCustom(), workouts.get(0).isCustom());
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnEmptyList_whenWorkoutBelongsToAnotherUser() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);
        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user1, Set.of(workout1, workout2));

        User user2 = dataHelper.createUser("two", role, country, null, 20);
        Workout workout3 = dataHelper.createWorkout(3, true, null);
        dataHelper.userAddWorkout(user2, Set.of(workout3));

        Workout workout4 = dataHelper.createWorkout(4, false, null);

        // When
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(workout3.getTitle(), user1.getId());

        // Then
        assertEquals(0, workouts.size());
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnEmptyList_whenWorkoutIsDefault() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);
        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user1, Set.of(workout1, workout2));

        User user2 = dataHelper.createUser("two", role, country, null, 20);
        Workout workout3 = dataHelper.createWorkout(3, true, null);
        dataHelper.userAddWorkout(user2, Set.of(workout3));

        Workout workout4 = dataHelper.createWorkout(4, false, null);

        // When
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(workout4.getTitle(), user1.getId());

        // Then
        assertEquals(0, workouts.size());
    }
}
