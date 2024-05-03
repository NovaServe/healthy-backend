package healthy.lifestyle.backend.reminder.workout.service;

import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderCreateRequestDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderResponseDto;
import org.springframework.data.domain.Page;

public interface WorkoutReminderService {
    WorkoutReminderResponseDto createWorkoutReminder(WorkoutReminderCreateRequestDto requestDto, long userId);

    Page<WorkoutReminderResponseDto> getWorkoutRemindersWithFilter(
            long userId, Boolean isActive, String sortField, String sortDirection, int pageSize, int pageNumber);
}
