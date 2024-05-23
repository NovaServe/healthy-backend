package healthy.lifestyle.backend.plan.workout.service;

import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.api.WorkoutApiImpl;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutPlanRepository;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.api.UserApiImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanServiceImplTest {
    @InjectMocks
    WorkoutPlanServiceImpl workoutPlanService;

    @Mock
    WorkoutPlanRepository workoutPlanRepository;

    @Mock
    WorkoutApiImpl workoutApi;

    @Mock
    UserApiImpl userApi;

    @Spy
    ModelMapper modelMapper;

    TestUtil dataUtil = new TestUtil();

    @Test
    void createWorkoutPlan_shouldCreateWorkoutPlanAndReturnResponseDto_whenValidRequest() {
    }

    @Test
    void createWorkoutPlan_shouldThrowException_whenWorkoutAlreadyHasActivePlan() {
    }

    @ParameterizedTest
    @MethodSource("createWorkoutPlanInvalidField")
    void createWorkoutPlan_shouldThrowException_whenInvalidRequest(
            Long workoutId, LocalDate startDate, LocalDate endDate) {

    }

    static Stream<Arguments> createWorkoutPlanInvalidField() {
        return Stream.of(
                // Invalid workoutId
                Arguments.of(1000L, "__-__-__", "__-__-__"),
                // Invalid start date
                Arguments.of(null, "__-__-__", "__-__-__"),
                // Invalid end date
                Arguments.of(null, "__-__-__", "__-__-__")
        );
    }

    @Test
    void createWorkoutPlan_shouldThrowException_whenInvalidJsonDescription() {
    }


}