package healthy.lifestyle.backend.plan.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutWithoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutPlanRepository;
import healthy.lifestyle.backend.shared.util.DateTimeService;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.shared.util.JsonUtil;
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import java.time.*;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutPlanServiceImpl implements WorkoutPlanService {
    @Autowired
    WorkoutApi workoutApi;

    @Autowired
    UserApi userApi;

    @Autowired
    WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    JsonUtil jsonUtil;

    @Autowired
    DateTimeService dateTimeService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId)
            throws JsonProcessingException {
        Map<String, Object> validated = validateCreateWorkoutPlan(requestDto, userId);
        User user = (User) validated.get("user");
        Workout workout = (Workout) validated.get("workout");

        List<JsonDescription> days =
                jsonUtil.deserializeJsonStringToJsonDescriptionList(requestDto.getJsonDescription());

        List<JsonDescription> daysProcessed = jsonUtil.processJsonDescription(
                days, ZoneId.of(user.getTimezone().getName()));

        WorkoutPlan workoutPlan = WorkoutPlan.builder()
                .startDate(dateTimeService.convertToDBDate(
                        requestDto.getStartDate(), user.getTimezone().getName()))
                .endDate(dateTimeService.convertToDBDate(
                        requestDto.getEndDate(), user.getTimezone().getName()))
                .jsonDescription(daysProcessed)
                .isActive(true)
                .createdAt(dateTimeService.getCurrentDatabaseZonedDateTime().toLocalDateTime())
                .deactivatedAt(null)
                .user(user)
                .workout(workout)
                .build();

        WorkoutPlan workoutPlanSaved = workoutPlanRepository.save(workoutPlan);
        WorkoutPlanResponseDto responseDto = WorkoutPlanResponseDto.builder()
                .id(workoutPlanSaved.getId())
                .workoutId(workout.getId())
                .startDate(dateTimeService.convertToUserDate(
                        workoutPlanSaved.getStartDate(), user.getTimezone().getName()))
                .endDate(dateTimeService.convertToUserDate(
                        workoutPlanSaved.getEndDate(), user.getTimezone().getName()))
                .jsonDescription(jsonUtil.serializeJsonDescriptionList(
                        workoutPlanSaved.getJsonDescription(),
                        user.getTimezone().getName()))
                .createdAt(dateTimeService.convertToUserDateTime(
                        workoutPlanSaved.getCreatedAt(), user.getTimezone().getName()))
                .build();

        return responseDto;
    }

    @Override
    public List<WorkoutWithoutPlanResponseDto> getDefaultAndCustomWorkoutsWithoutPlans(long userId) {
        List<Object[]> workouts = new ArrayList<>();
        workouts.addAll(workoutPlanRepository.getDefaultWorkoutsWithoutPlans(userId));
        workouts.addAll(workoutPlanRepository.getCustomWorkoutsWithoutPlans(userId));
        List<WorkoutWithoutPlanResponseDto> responseDtoList = workouts.stream()
                .map(elt -> WorkoutWithoutPlanResponseDto.builder()
                        .id((long) elt[0])
                        .title((String) elt[1])
                        .build())
                .toList();
        return responseDtoList;
    }

    private Map<String, Object> validateCreateWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId) {
        User user = userApi.getUserById(userId);
        if (user == null) {
            throw new ApiException(ErrorMessage.USER_NOT_FOUND, userId, HttpStatus.BAD_REQUEST);
        }

        Workout workout = workoutApi.getWorkoutById(requestDto.getWorkoutId());
        if (workout == null) {
            throw new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, requestDto.getWorkoutId(), HttpStatus.BAD_REQUEST);
        }

        // Only one active workout plan association is allowed
        List<WorkoutPlan> plans = workoutPlanRepository.findByUserIdAndWorkoutId(userId, requestDto.getWorkoutId());
        if (!plans.isEmpty()) {
            for (WorkoutPlan plan : plans) {
                if (plan.getIsActive()) {
                    throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.CONFLICT);
                }
            }
        }

        // StartDate cannot be in the past
        LocalDate startDateDBZone = dateTimeService.convertToDBDate(
                requestDto.getStartDate(), user.getTimezone().getName());
        LocalDate currentDBDate = dateTimeService.getCurrentDBDate();
        if (startDateDBZone.isBefore(currentDBDate)) {
            throw new ApiException(ErrorMessage.INCORRECT_TIME, null, HttpStatus.BAD_REQUEST);
        }

        // EndDate cannot be earlier than StartDate
        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new ApiException(ErrorMessage.INCORRECT_TIME, null, HttpStatus.BAD_REQUEST);
        }

        return new HashMap<>() {
            {
                put("user", user);
                put("workout", workout);
            }
        };
    }
}
