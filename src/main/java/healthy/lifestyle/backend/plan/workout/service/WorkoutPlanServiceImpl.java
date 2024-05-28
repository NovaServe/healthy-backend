package healthy.lifestyle.backend.plan.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.calendar.shared.service.DateTimeService;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlanDayId;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutPlanRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.shared.util.JsonUtil;
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;
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
    ModelMapper modelMapper;

    @Autowired
    JsonUtil jsonUtil;

    @Override
    @Transactional
    public WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId) {

        //A plan can only be created by an existing user
        User user = userApi.getUserById(userId);
        if(user == null){throw new ApiException(ErrorMessage.USER_NOT_FOUND, userId, HttpStatus.BAD_REQUEST);}

        // The plan can only be for an existing workout
        Workout workout = workoutApi.getWorkoutById(requestDto.getWorkoutId());
        if(workout == null){throw new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, requestDto.getWorkoutId(), HttpStatus.BAD_REQUEST);}

        // Only one active workout plan association is allowed
        List<WorkoutPlan> plans = workoutPlanRepository.findByUserIdAndWorkoutId(userId, requestDto.getWorkoutId());
        if(!plans.isEmpty()){
            for(WorkoutPlan plan : plans){
                if(plan.getIsActive()){throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.CONFLICT);}
            }
        }

        // StartDate and EndDate must be specified
        if(requestDto.getStartDate() == null || requestDto.getEndDate() == null){
            throw new ApiException(ErrorMessage.INCORRECT_TIME, null, HttpStatus.BAD_REQUEST);
        }

        // EndDate cannot be earlier than StartDate
        if(requestDto.getEndDate().isBefore(requestDto.getStartDate())){
            throw new ApiException(ErrorMessage.INCORRECT_TIME, null, HttpStatus.BAD_REQUEST);
        }

        List<JsonDescription> days;

        try {days = jsonUtil.deserializeJsonStringToJsonDescriptionList(requestDto.getJsonDescription());}
        catch (JsonProcessingException e) {throw new RuntimeException(e);}

        days = jsonUtil.processJsonDescription(days, ZoneId.of(user.getTimezone().getName()));

        ZonedDateTime startZonedDateTime =
                requestDto.getStartDate().atZone(ZoneId.of(user.getTimezone().getName())).withZoneSameInstant(ZoneId.of("Europe/London"));
        ZonedDateTime endZonedDateTime =
                requestDto.getEndDate().atZone(ZoneId.of(user.getTimezone().getName())).withZoneSameInstant(ZoneId.of("Europe/London"));

        WorkoutPlan workoutPlan = WorkoutPlan.builder()
                .startDate(startZonedDateTime.toLocalDateTime())
                .endDate(endZonedDateTime.toLocalDateTime())
                .jsonDescription(days)
                .isActive(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime())
                .deactivatedAt(null)
                .user(user)
                .workout(workout)
                .build();

        workoutPlanRepository.save(workoutPlan);

        WorkoutPlanResponseDto responseDto = modelMapper.map(requestDto, WorkoutPlanResponseDto.class);
        responseDto.setCreatedAt(ZonedDateTime.now(ZoneId.of(user.getTimezone().getName())).toLocalDateTime());

        return responseDto;
    }
}
