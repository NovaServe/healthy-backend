package healthy.lifestyle.backend.reminder.workout.controller;

import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderCreateRequestDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderResponseDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderUpdateRequestDto;
import healthy.lifestyle.backend.reminder.workout.service.WorkoutReminderService;
import healthy.lifestyle.backend.user.service.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/calendar/workouts")
public class WorkoutReminderController {

    @Autowired
    AuthUtil authUtil;

    @Autowired
    WorkoutReminderService workoutReminderService;

    @PostMapping("/reminders")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutReminderResponseDto> createWorkoutReminder(
            @Valid @RequestBody WorkoutReminderCreateRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutReminderResponseDto responseDto = workoutReminderService.createWorkoutReminder(requestDto, userId);

        // call update tasks
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
