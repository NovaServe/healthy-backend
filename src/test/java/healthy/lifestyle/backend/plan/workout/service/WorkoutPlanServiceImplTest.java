package healthy.lifestyle.backend.plan.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.activity.workout.api.WorkoutApiImpl;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutPlanRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.shared.util.JsonUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.api.UserApiImpl;
import healthy.lifestyle.backend.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanServiceImplTest {
    @InjectMocks
    WorkoutPlanServiceImpl workoutPlanService;

    @Mock
    WorkoutPlanRepository workoutPlanRepository;

    @Mock
    WorkoutDayIdRepository workoutDayIdRepository;

    @Mock
    WorkoutApiImpl workoutApi;

    @Mock
    UserApiImpl userApi;

    @Mock
    JsonUtil jsonUtil;

    TestUtil dataUtil = new TestUtil();

    @Spy
    private DtoUtil dtoUtil;

    @Test
    void createWorkoutPlan_shouldCreateWorkoutPlanAndReturnResponseDto_whenValidRequest() {
        // Given

        int seed = 1;

        User user = dataUtil.createUser(seed);

        Workout workout = dataUtil.createDefaultWorkout(seed);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, workout.getId());
        List<JsonDescription> expectedJsonDescriptions = List.of(dataUtil.createJsonDescription(seed));

        when(userApi.getUserById(user.getId())).thenReturn(user);
        when(workoutApi.getWorkoutById(anyLong())).thenReturn(workout);

        try {
            doReturn(expectedJsonDescriptions)
                    .when(jsonUtil)
                    .deserializeJsonStringToJsonDescriptionList(requestDto.getJsonDescription());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // When
        WorkoutPlanResponseDto responseDto = workoutPlanService.createWorkoutPlan(requestDto, user.getId());

        // Then
        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(requestDto);
    }

    @Test
    void createWorkoutPlan_shouldThrowException_whenWorkoutAlreadyHasActivePlan() {
        // Given
        int seed = 1;
        User user = dataUtil.createUser(seed);

        Workout workout = dataUtil.createDefaultWorkout(seed);
        WorkoutPlan savedWorkoutPlan = dataUtil.createWorkoutPlan(Long.valueOf(seed), user, workout);

        doReturn(user).when(userApi).getUserById(anyLong());
        doReturn(workout).when(workoutApi).getWorkoutById(anyLong());
        when(workoutPlanRepository.findByUserIdAndWorkoutId(user.getId(), workout.getId()))
                .thenReturn(List.of(savedWorkoutPlan));

        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, workout.getId());

        ApiException expectedException = new ApiException(ErrorMessage.ALREADY_EXISTS, 1L, HttpStatus.CONFLICT);

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));

        // Then
        verify(workoutPlanRepository, times(1)).findByUserIdAndWorkoutId(user.getId(), workout.getId());
        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
    }

    @ParameterizedTest
    @MethodSource("createWorkoutPlanInvalidField")
    void createWorkoutPlan_shouldThrowException_whenInvalidRequest(
            Long workoutId, LocalDateTime startDate, LocalDateTime endDate) {

        // Given
        int seed = 1;
        User user = dataUtil.createUser(seed);

        Workout savedWorkout = dataUtil.createDefaultWorkout(seed);

        JsonDescription jsonDescription = dataUtil.createJsonDescription(seed);
        String jsonDescriptionStringified;

        try {
            jsonDescriptionStringified = dataUtil.serializeJsonDescriptionList(List.of(jsonDescription));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        WorkoutPlanCreateRequestDto requestDto = WorkoutPlanCreateRequestDto.builder()
                .workoutId(workoutId)
                .startDate(startDate)
                .endDate(endDate)
                .jsonDescription(jsonDescriptionStringified)
                .build();

        when(userApi.getUserById(user.getId())).thenReturn(user);
        doAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    return savedWorkout.getId().equals(id) ? savedWorkout : null;
                })
                .when(workoutApi)
                .getWorkoutById(anyLong());

        // Then
        WorkoutPlanResponseDto responseDto;
        if (workoutId != null) {
            if (workoutId == savedWorkout.getId()) {
                responseDto = workoutPlanService.createWorkoutPlan(requestDto, user.getId());
                assertEquals(requestDto.getWorkoutId(), responseDto.getWorkoutId());
            } else {
                ApiException expectedException =
                        new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, workoutId, HttpStatus.BAD_REQUEST);
                ApiException actualException = assertThrows(
                        ApiException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));
                assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
                assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
            }
        } else {
            requestDto.setWorkoutId(savedWorkout.getId());
            ApiException expectedException = new ApiException(ErrorMessage.INCORRECT_TIME, 1L, HttpStatus.BAD_REQUEST);
            ApiException actualException = assertThrows(
                    ApiException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));
            assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
            assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
        }
    }

    static Stream<Arguments> createWorkoutPlanInvalidField() {

        return Stream.of(
                // Invalid workoutId
                Arguments.of(
                        1000L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(7)),
                // Invalid start date
                Arguments.of(
                        null,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(7)),
                // Invalid end date
                Arguments.of(null, LocalDateTime.now().plusDays(1), LocalDateTime.now()));
    }

    @Test
    void createWorkoutPlan_shouldThrowException_whenInvalidJsonDescription() {

        int seed = 1;

        User user = dataUtil.createUser(seed);
        Workout workout = dataUtil.createDefaultWorkout(seed);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, workout.getId());

        doReturn(user).when(userApi).getUserById(anyLong());
        doReturn(workout).when(workoutApi).getWorkoutById(anyLong());

        try {
            doThrow(JsonProcessingException.class)
                    .when(jsonUtil)
                    .deserializeJsonStringToJsonDescriptionList(anyString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Throwable exception = assertThrows(
                RuntimeException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));

        assertTrue(exception.getCause() instanceof JsonProcessingException);
    }
}
