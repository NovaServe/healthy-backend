package healthy.lifestyle.backend.plan.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
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

        // Only one workout-plan association is allowed
        workoutPlanRepository
                .findByUserIdAndWorkoutId(userId, requestDto.getWorkoutId())
                .ifPresent(plan -> {
                    throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.BAD_REQUEST);
                });

        User user = userApi.getUserById(userId);
        Workout workout = workoutApi.getWorkoutById(requestDto.getWorkoutId());
        WorkoutPlan workoutPlan;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode days = mapper.readTree(requestDto.getJsonDescription());

            ZoneId srcZoneId = ZoneId.of(user.getTimezone().getName());
            ZoneId destZoneId = ZoneId.of("Europe/London");

            ZonedDateTime startZonedDateTime =
                    requestDto.getStartDate().atZone(srcZoneId).withZoneSameInstant(destZoneId);
            ZonedDateTime endZonedDateTime =
                    requestDto.getEndDate().atZone(srcZoneId).withZoneSameInstant(destZoneId);

            for (JsonNode day : days) {

                // Generate unique id for plan day
                WorkoutPlanDayId dayId = new WorkoutPlanDayId();
                dayId.setJson_id(1L);
                dayId = workoutDayIdRepository.save(dayId);
                // After that just delete this entity from database after update counter
                workoutDayIdRepository.delete(dayId);

                // Convert day and time from user timezone to server timezone
                DayOfWeek dayOfWeek =
                        DayOfWeek.valueOf(day.get("week_day").asText().toUpperCase(Locale.ENGLISH));
                LocalDateTime targetDate = requestDto.getStartDate().with(dayOfWeek);
                LocalTime localTime = LocalTime.of(
                        Integer.parseInt(day.get("hours").toString()),
                        Integer.parseInt(day.get("minutes").toString()));
                LocalDateTime localDateTime = LocalDateTime.of(targetDate.toLocalDate(), localTime);

                ZonedDateTime sourceZonedDateTime = localDateTime.atZone(srcZoneId);
                ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(destZoneId);

                // Update json day
                ((ObjectNode) day).put("json_id", dayId.getId().toString());
                ((ObjectNode) day)
                        .put(
                                "week_day",
                                ((DayOfWeek) targetZonedDateTime.getDayOfWeek())
                                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                ((ObjectNode) day).put("hours", targetZonedDateTime.getHour());
                ((ObjectNode) day).put("minutes", targetZonedDateTime.getMinute());
            }

            workoutPlan = WorkoutPlan.builder()
                    .startDate(startZonedDateTime.toLocalDateTime())
                    .endDate(endZonedDateTime.toLocalDateTime())
                    .jsonDescription(days)
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
