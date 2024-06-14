package healthy.lifestyle.backend.plan.workout.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.activity.workout.api.WorkoutApiImpl;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutPlanRepository;
import healthy.lifestyle.backend.shared.util.DateTimeService;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.shared.util.JsonUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.SharedUtil;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.api.UserApiImpl;
import healthy.lifestyle.backend.user.model.User;
import java.time.LocalDate;
import java.util.Collections;
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
import org.modelmapper.ModelMapper;
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

    @Spy
    ModelMapper modelMapper;

    @Spy
    DateTimeService dateTimeService;

    @Spy
    TestUtil testUtil;

    @Spy
    DtoUtil dtoUtil;

    @Test
    void createWorkoutPlan_shouldCreateWorkoutPlanAndReturnResponseDto_whenValidRequest()
            throws JsonProcessingException {
        // Given
        int seed = 1;
        User user = testUtil.createUser(seed);
        Workout workout = testUtil.createDefaultWorkout(seed);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, workout.getId());
        List<JsonDescription> mockJsonDescription = List.of(SharedUtil.createJsonDescription(1));

        when(userApi.getUserById(user.getId())).thenReturn(user);
        when(workoutApi.getWorkoutById(workout.getId())).thenReturn(workout);
        when(workoutPlanRepository.findByUserIdAndWorkoutId(user.getId(), requestDto.getWorkoutId()))
                .thenReturn(Collections.EMPTY_LIST);
        when(jsonUtil.deserializeJsonStringToJsonDescriptionList(anyString())).thenReturn(mockJsonDescription);
        when(jsonUtil.processJsonDescription(anyList(), any())).thenReturn(mockJsonDescription);
        when(jsonUtil.serializeJsonDescriptionList(anyList(), anyString())).thenReturn("{}");
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenAnswer(invocation -> {
            WorkoutPlan workoutPlan = (WorkoutPlan) invocation.getArguments()[0];
            workoutPlan.setId(1L);
            return workoutPlan;
        });

        // When
        WorkoutPlanResponseDto responseDto = workoutPlanService.createWorkoutPlan(requestDto, user.getId());

        // Then
        verify(userApi, times(1)).getUserById(user.getId());
        verify(workoutApi, times(1)).getWorkoutById(workout.getId());
        verify(workoutPlanRepository, times(1)).findByUserIdAndWorkoutId(user.getId(), workout.getId());
        verify(jsonUtil, times(1)).deserializeJsonStringToJsonDescriptionList(anyString());
        verify(jsonUtil, times(1)).processJsonDescription(anyList(), any());
        verify(jsonUtil, times(1)).serializeJsonDescriptionList(anyList(), anyString());

        assertEquals(workout.getId(), responseDto.getWorkoutId());
        assertEquals(requestDto.getStartDate(), responseDto.getStartDate());
        assertEquals(requestDto.getEndDate(), responseDto.getEndDate());
        assertNotNull(responseDto.getJsonDescription());
        assertNotNull(responseDto.getCreatedAt());
    }

    @Test
    void createWorkoutPlan_shouldThrowException_whenWorkoutAlreadyHasActivePlan() {
        // Given
        int seed = 1;
        User user = testUtil.createUser(seed);
        Workout workout = testUtil.createDefaultWorkout(seed);
        WorkoutPlan workoutPlanAlreadyExists = testUtil.createWorkoutPlan((long) seed, user, workout);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, workout.getId());
        ApiException expectedException = new ApiException(ErrorMessage.ALREADY_EXISTS, 1L, HttpStatus.CONFLICT);

        when(userApi.getUserById(user.getId())).thenReturn(user);
        when(workoutApi.getWorkoutById(anyLong())).thenReturn(workout);
        when(workoutPlanRepository.findByUserIdAndWorkoutId(user.getId(), workout.getId()))
                .thenReturn(List.of(workoutPlanAlreadyExists));

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));

        // Then
        verify(userApi, times(1)).getUserById(user.getId());
        verify(workoutApi, times(1)).getWorkoutById(workout.getId());
        verify(workoutPlanRepository, times(1)).findByUserIdAndWorkoutId(user.getId(), workout.getId());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
    }

    @Test
    void createWorkoutPlan_shouldThrowException_whenWorkoutNotFound() {
        // Given
        int seed = 1;
        User user = testUtil.createUser(seed);
        long wrongWorkoutId = 1000L;
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, wrongWorkoutId);
        ApiException expected = new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, null, HttpStatus.BAD_REQUEST);

        when(userApi.getUserById(user.getId())).thenReturn(user);
        when(workoutApi.getWorkoutById(wrongWorkoutId)).thenReturn(null);

        // When
        ApiException actual =
                assertThrows(ApiException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));

        // Then
        verify(userApi, times(1)).getUserById(user.getId());
        verify(workoutApi, times(1)).getWorkoutById(wrongWorkoutId);
        verify(workoutPlanRepository, times(0)).findByUserIdAndWorkoutId(user.getId(), wrongWorkoutId);

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(expected.getHttpStatus(), actual.getHttpStatus());
    }

    @ParameterizedTest
    @MethodSource("createWorkoutPlanInvalidDate")
    void createWorkoutPlan_shouldThrowException_whenInvalidDate(LocalDate startDate, LocalDate endDate) {
        // Given
        int seed = 1;
        User user = testUtil.createUser(seed);
        Workout workout = testUtil.createDefaultWorkout(seed);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(seed, workout.getId());
        requestDto.setStartDate(startDate);
        requestDto.setEndDate(endDate);

        when(userApi.getUserById(user.getId())).thenReturn(user);
        when(workoutApi.getWorkoutById(workout.getId())).thenReturn(workout);

        ApiException expected = new ApiException(ErrorMessage.INCORRECT_TIME, null, HttpStatus.BAD_REQUEST);

        // When
        ApiException actual =
                assertThrows(ApiException.class, () -> workoutPlanService.createWorkoutPlan(requestDto, user.getId()));

        // Then
        verify(userApi, times(1)).getUserById(user.getId());
        verify(workoutApi, times(1)).getWorkoutById(workout.getId());
        verify(workoutPlanRepository, times(1)).findByUserIdAndWorkoutId(user.getId(), workout.getId());

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(expected.getHttpStatus(), actual.getHttpStatus());
    }

    static Stream<Arguments> createWorkoutPlanInvalidDate() {
        return Stream.of(
                // Invalid start date
                Arguments.of(LocalDate.now().minusDays(1), LocalDate.now().plusDays(7)),
                // Invalid end date
                Arguments.of(LocalDate.now().plusDays(1), LocalDate.now()));
    }
}
