package healthy.lifestyle.backend.calendar.controller;

import healthy.lifestyle.backend.calendar.dto.ActivityDayDto;
import healthy.lifestyle.backend.calendar.dto.ActivityRowDto;
import healthy.lifestyle.backend.calendar.dto.ActivityWeekDto;
import healthy.lifestyle.backend.calendar.model.ActivityType;
import healthy.lifestyle.backend.user.service.AuthUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/calendar")
public class CalendarController {
    @Autowired
    AuthUtil authUtil;

    @GetMapping("/activity/today")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ActivityDayDto> getTodayActivity() {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        List<ActivityRowDto> activities = new ArrayList<>();
        activities.add(ActivityRowDto.builder()
                .activityId(1)
                .activityType(ActivityType.WORKOUT)
                .isSingle(true)
                .isPausedBilling(false)
                .isCompleted(true)
                .title("Workout 1")
                .hours(10)
                .minutes(30)
                .build());

        activities.add(ActivityRowDto.builder()
                .activityId(1)
                .activityType(ActivityType.MENTAL)
                .isSingle(true)
                .isPausedBilling(true)
                .isCompleted(false)
                .title("Mental 1")
                .hours(11)
                .minutes(40)
                .build());

        activities.add(ActivityRowDto.builder()
                .activityId(2)
                .activityType(ActivityType.WORKOUT)
                .isSingle(false)
                .isPausedBilling(true)
                .isCompleted(false)
                .title("Workout 2")
                .hours(16)
                .minutes(0)
                .build());

        LocalDate dayDate = LocalDate.of(2024, 2, 1);
        ActivityDayDto activityDayDto = ActivityDayDto.builder()
                .dayDate(dayDate)
                .dayOfWeek(dayDate.getDayOfWeek())
                .activities(activities)
                .build();

        return ResponseEntity.ok(activityDayDto);
    }

    @GetMapping("/activity/week")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ActivityWeekDto> getWeekActivity() {

        List<ActivityRowDto> activitiesMonday = new ArrayList<>();
        activitiesMonday.add(ActivityRowDto.builder()
                .activityId(1)
                .activityType(ActivityType.WORKOUT)
                .isSingle(true)
                .isPausedBilling(false)
                .isCompleted(false)
                .title("Workout 1")
                .hours(10)
                .minutes(30)
                .build());

        activitiesMonday.add(ActivityRowDto.builder()
                .activityId(2)
                .activityType(ActivityType.WORKOUT)
                .isSingle(false)
                .isPausedBilling(false)
                .isCompleted(false)
                .title("Workout 2")
                .hours(16)
                .minutes(30)
                .build());

        LocalDate dayMonday = LocalDate.of(2024, 2, 1);
        ActivityDayDto monday = ActivityDayDto.builder()
                .dayDate(dayMonday)
                .dayOfWeek(dayMonday.getDayOfWeek())
                .activities(activitiesMonday)
                .build();

        List<ActivityRowDto> activitiesWednesday = new ArrayList<>();
        activitiesWednesday.add(ActivityRowDto.builder()
                .activityId(1)
                .activityType(ActivityType.WORKOUT)
                .isSingle(true)
                .isPausedBilling(false)
                .isCompleted(false)
                .title("Workout 1")
                .hours(10)
                .minutes(30)
                .build());

        activitiesWednesday.add(ActivityRowDto.builder()
                .activityId(2)
                .activityType(ActivityType.WORKOUT)
                .isSingle(false)
                .isPausedBilling(false)
                .isCompleted(false)
                .title("Workout 2")
                .hours(16)
                .minutes(30)
                .build());

        LocalDate dayWednesday = LocalDate.of(2024, 2, 3);
        ActivityDayDto wednesday = ActivityDayDto.builder()
                .dayDate(dayWednesday)
                .dayOfWeek(dayWednesday.getDayOfWeek())
                .activities(activitiesWednesday)
                .build();

        ActivityWeekDto activityWeekDto = ActivityWeekDto.builder()
                .weekStartDate(LocalDate.of(2024, 2, 1))
                .weekEndDate(LocalDate.of(2024, 2, 8))
                .monday(monday)
                .wednesday(wednesday)
                .build();

        return ResponseEntity.ok(activityWeekDto);
    }
}
