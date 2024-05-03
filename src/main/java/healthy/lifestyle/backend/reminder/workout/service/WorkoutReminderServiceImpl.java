package healthy.lifestyle.backend.reminder.workout.service;

import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.calendar.shared.service.DateTimeService;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderCreateRequestDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderResponseDto;
import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminder;
import healthy.lifestyle.backend.reminder.workout.repository.WorkoutReminderRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutReminderServiceImpl implements WorkoutReminderService {

    @Autowired
    WorkoutApi workoutApi;

    @Autowired
    UserApi userApi;

    @Autowired
    DateTimeService dateTimeService;

    @Autowired
    WorkoutReminderRepository workoutReminderRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public WorkoutReminderResponseDto createWorkoutReminder(WorkoutReminderCreateRequestDto requestDto, long userId) {

        // Only one workout-reminder association is allowed
        workoutReminderRepository
                .findByUserIdAndWorkoutId(userId, requestDto.getWorkoutId())
                .ifPresent(reminder -> {
                    throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.BAD_REQUEST);
                });
        User user = userApi.getUserById(userId);
        Workout workout = workoutApi.getWorkoutById(requestDto.getWorkoutId());

        WorkoutReminder workoutReminder = WorkoutReminder.builder()
                .xmlDescription(requestDto.getXml_description())
                .isActive(true)
                .isPaused(false)
                .createdAt(dateTimeService.getCurrentDate())
                .workout(workout)
                .user(user)
                .build();

        WorkoutReminder workoutReminderSaved = workoutReminderRepository.save(workoutReminder);

        WorkoutReminderResponseDto responseDto =
                modelMapper.map(workoutReminderSaved, WorkoutReminderResponseDto.class);

        return responseDto;
    }

    @Override
    @Transactional
    public Page<WorkoutReminderResponseDto> getWorkoutRemindersWithFilter(
            long userId, Boolean isActive, String sortField, String sortDirection, int pageSize, int pageNumber) {

        Pageable pageable =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        Page<WorkoutReminder> workoutRemindersPage = workoutReminderRepository.findByUserId(userId, isActive, pageable);

        Page<WorkoutReminderResponseDto> responseDtoPage = workoutRemindersPage.map(workoutReminder -> {
            WorkoutReminderResponseDto responseDto = modelMapper.map(workoutReminder, WorkoutReminderResponseDto.class);
            return responseDto;
        });

        return responseDtoPage;
    }
}
