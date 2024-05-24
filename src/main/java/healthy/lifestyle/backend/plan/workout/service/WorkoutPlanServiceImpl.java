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
    DateTimeService dateTimeService;

    @Autowired
    WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    WorkoutDayIdRepository workoutDayIdRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId) {

        // Only one active workout plan association is allowed
        List<WorkoutPlan> plans = workoutPlanRepository.findByUserIdAndWorkoutId(userId, requestDto.getWorkoutId());
        if(!plans.isEmpty()){
            for(WorkoutPlan plan : plans){
                if(plan.getIsActive()){throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.BAD_REQUEST);}
            }
        }

        User user = userApi.getUserById(userId);
        Workout workout = workoutApi.getWorkoutById(requestDto.getWorkoutId());
        WorkoutPlan workoutPlan;

        try {

            JsonUtil jsonUtil = new JsonUtil();
            List<JsonDescription> jsonDescriptions = jsonUtil.deserializeJsonStringToJsonDescriptionList(requestDto.getJsonDescription());

            ZoneId srcZoneId = ZoneId.of(user.getTimezone().getName());
            ZoneId destZoneId = ZoneId.of("Europe/London");

            ZonedDateTime startZonedDateTime =
                    requestDto.getStartDate().atZone(srcZoneId).withZoneSameInstant(destZoneId);
            ZonedDateTime endZonedDateTime =
                    requestDto.getEndDate().atZone(srcZoneId).withZoneSameInstant(destZoneId);

            for (JsonDescription desc : jsonDescriptions) {

                // Generate unique id for plan day
                WorkoutPlanDayId dayId = new WorkoutPlanDayId();
                dayId.setJson_id(1L);
                dayId = workoutDayIdRepository.save(dayId);
                // After that just delete this entity from database after update counter
                workoutDayIdRepository.delete(dayId);

                // Convert day and time from user timezone to server timezone
                DayOfWeek dayOfWeek = desc.getDayOfWeek();
                LocalDateTime targetDate = requestDto.getStartDate().with(dayOfWeek);
                LocalTime localTime = LocalTime.of(desc.getHours(), desc.getMinutes());
                LocalDateTime localDateTime = LocalDateTime.of(targetDate.toLocalDate(), localTime);

                ZonedDateTime sourceZonedDateTime = localDateTime.atZone(srcZoneId);
                ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(destZoneId);

                // Update day
                desc.setJson_id(dayId.getId());
                desc.setDayOfWeek((DayOfWeek) targetZonedDateTime.getDayOfWeek());
                desc.setHours(targetZonedDateTime.getHour());
                desc.setMinutes(targetZonedDateTime.getMinute());
            }

            workoutPlan = WorkoutPlan.builder()
                    .startDate(startZonedDateTime.toLocalDateTime())
                    .endDate(endZonedDateTime.toLocalDateTime())
                    .jsonDescription(jsonDescriptions)
                    .isActive(true)
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .deactivatedAt(null)
                    .user(user)
                    .workout(workout)
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        WorkoutPlan workoutPlanSaved = workoutPlanRepository.save(workoutPlan);

        WorkoutPlanResponseDto responseDto = modelMapper.map(workoutPlanSaved, WorkoutPlanResponseDto.class);

        return responseDto;
    }
}
