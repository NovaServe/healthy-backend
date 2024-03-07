package healthy.lifestyle.backend.activity.workout.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class WorkoutRepositoryTest {
    @MockBean
    FirebaseMessaging firebaseMessaging;

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
    WorkoutRepository workoutRepository;

    @Autowired
    DbUtil dbUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnCustomWorkout() {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user1);
        boolean exerciseNeedsEquipment = false;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, exerciseNeedsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise1 = dbUtil.createCustomExercise(
                2, exerciseNeedsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user1);
        Workout customWorkout1 = dbUtil.createCustomWorkout(1, List.of(defaultExercise1, customExercise1), user1);

        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(3, exerciseNeedsEquipment, List.of(bodyPart3), List.of(defaultHttpRef2));
        Exercise customExercise2 = dbUtil.createCustomExercise(
                4, exerciseNeedsEquipment, List.of(bodyPart4), List.of(customHttpRef2), user2);
        Workout customWorkout2 = dbUtil.createCustomWorkout(2, List.of(defaultExercise2, customExercise2), user2);

        // When
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(customWorkout1.getTitle(), user1.getId());

        // Then
        assertEquals(1, workouts.size());
        assertEquals(customWorkout1.getId(), workouts.get(0).getId());
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnEmptyList_whenWorkoutBelongsToAnotherUser() {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user1);
        boolean exerciseNeedsEquipment = false;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, exerciseNeedsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise1 = dbUtil.createCustomExercise(
                2, exerciseNeedsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user1);
        Workout customWorkout1 = dbUtil.createCustomWorkout(1, List.of(defaultExercise1, customExercise1), user1);

        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(3, exerciseNeedsEquipment, List.of(bodyPart3), List.of(defaultHttpRef2));
        Exercise customExercise2 = dbUtil.createCustomExercise(
                4, exerciseNeedsEquipment, List.of(bodyPart4), List.of(customHttpRef2), user2);
        Workout customWorkout2 = dbUtil.createCustomWorkout(2, List.of(defaultExercise2, customExercise2), user2);

        // When
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(customWorkout1.getTitle(), user2.getId());

        // Then
        assertEquals(0, workouts.size());
    }

    @Test
    void findCustomByTitleAndUserIdTest_shouldReturnEmptyList_whenWorkoutIsDefault() {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        boolean exerciseNeedsEquipment = false;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, exerciseNeedsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise1 = dbUtil.createCustomExercise(
                2, exerciseNeedsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise1, customExercise1), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(3);
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(3, exerciseNeedsEquipment, List.of(bodyPart3), List.of(defaultHttpRef2));
        Workout defaultWorkout = dbUtil.createDefaultWorkout(2, List.of(defaultExercise2));

        // When
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(defaultWorkout.getTitle(), user.getId());

        // Then
        assertEquals(0, workouts.size());
    }
}
