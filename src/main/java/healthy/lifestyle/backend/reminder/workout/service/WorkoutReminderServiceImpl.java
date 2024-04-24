package healthy.lifestyle.backend.reminder.workout.service;

import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.calendar.dto.DayRequestDto;
import healthy.lifestyle.backend.calendar.dto.DayResponseDto;
import healthy.lifestyle.backend.calendar.model.ReminderType;
import healthy.lifestyle.backend.calendar.shared.service.DateTimeService;
import healthy.lifestyle.backend.notification.scheduler.NotificationService;
import healthy.lifestyle.backend.notification.scheduler.TaskDto;
import healthy.lifestyle.backend.notification.shared.ActivityType;
import healthy.lifestyle.backend.notification.shared.NotificationType;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderCreateRequestDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderResponseDto;
import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminder;
import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminderDay;
import healthy.lifestyle.backend.reminder.workout.repository.WorkoutReminderDayRepository;
import healthy.lifestyle.backend.reminder.workout.repository.WorkoutReminderRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
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
    WorkoutReminderDayRepository workoutReminderDayRepository;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ModelMapper modelMapper;

    private static final int NOTIFY_BEFORE_IN_MINUTES_DEFAULT = 5;

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
                .reminderType(requestDto.getReminderType())
                .startDate(requestDto.getStartDate())
                .isActive(true)
                .isPaused(false)
                .notifyBeforeInMinutes(requestDto.getNotifyBeforeInMinutes())
                .createdAt(dateTimeService.getCurrentDate())
                .workout(workout)
                .user(user)
                .build();

        if (requestDto.getEndDate() == null && requestDto.getApplyDays() == null) {
            throw new ApiExceptionCustomMessage("End date or Apply days should be specified", HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getEndDate() != null) {
            // TODO: requestDto time is in user's timezone, date must be stored in
            //  the database in the server's timezone
            workoutReminder.setEndDate(requestDto.getEndDate());
        } else {
            LocalDate endDate = requestDto.getStartDate().plusDays(requestDto.getApplyDays());
            workoutReminder.setEndDate(endDate);
        }

        WorkoutReminder workoutReminderSaved = workoutReminderRepository.save(workoutReminder);
        setupWorkoutReminderDays(workoutReminderSaved, requestDto);

        WorkoutReminderResponseDto responseDto =
                modelMapper.map(workoutReminderSaved, WorkoutReminderResponseDto.class);

        responseDto.setDays(new ArrayList<>());
        workoutReminderSaved.getDaysSortedById().forEach(day -> {
            DayResponseDto dayResponseDto = modelMapper.map(day, DayResponseDto.class);
            responseDto.getDays().add(dayResponseDto);
        });

        updateScheduler(workoutReminderSaved);

        return responseDto;
    }

    @Transactional
    private void setupWorkoutReminderDays(
            WorkoutReminder workoutReminderSaved, WorkoutReminderCreateRequestDto requestDto) {

        if (workoutReminderSaved.getReminderType().equals(ReminderType.DAILY)
                || workoutReminderSaved.getReminderType().equals(ReminderType.DAY_AFTER_DAY)) {

            int hours = requestDto.getDays().get(0).getHours();
            int minutes = requestDto.getDays().get(0).getMinutes();

            Map<String, Integer> timeNotifyBeforeDefault =
                    dateTimeService.subtractMinutes(hours, minutes, NOTIFY_BEFORE_IN_MINUTES_DEFAULT);

            WorkoutReminderDay workoutReminderDay = WorkoutReminderDay.builder()
                    .hours(requestDto.getDays().get(0).getHours())
                    .minutes(requestDto.getDays().get(0).getMinutes())
                    .hoursNotifyBeforeDefault(timeNotifyBeforeDefault.get("hours"))
                    .minutesNotifyBeforeDefault(timeNotifyBeforeDefault.get("minutes"))
                    .isActive(true)
                    .createdAt(dateTimeService.getCurrentDate())
                    .build();

            if (requestDto.getNotifyBeforeInMinutes() != null) {
                Map<String, Integer> timeNotifyBefore =
                        dateTimeService.subtractMinutes(hours, minutes, requestDto.getNotifyBeforeInMinutes());
                workoutReminderDay.setHoursNotifyBefore(timeNotifyBefore.get("hours"));
                workoutReminderDay.setMinutesNotifyBefore(timeNotifyBefore.get("minutes"));
            }

            workoutReminderDay.setWorkoutReminder(workoutReminderSaved);
            WorkoutReminderDay workoutReminderDaySaved = workoutReminderDayRepository.save(workoutReminderDay);
            workoutReminderSaved.addReminderDays(List.of(workoutReminderDaySaved));
        }

        if (workoutReminderSaved.getReminderType().equals(ReminderType.ONCE)) {

            int hours = requestDto.getDays().get(0).getHours();
            int minutes = requestDto.getDays().get(0).getMinutes();

            Map<String, Integer> timeNotifyBeforeDefault =
                    dateTimeService.subtractMinutes(hours, minutes, NOTIFY_BEFORE_IN_MINUTES_DEFAULT);

            WorkoutReminderDay workoutReminderDay = WorkoutReminderDay.builder()
                    .day(requestDto.getDays().get(0).getDay())
                    .hours(hours)
                    .minutes(minutes)
                    .hoursNotifyBeforeDefault(timeNotifyBeforeDefault.get("hours"))
                    .minutesNotifyBeforeDefault(timeNotifyBeforeDefault.get("minutes"))
                    .isActive(true)
                    .createdAt(dateTimeService.getCurrentDate())
                    .build();

            if (requestDto.getNotifyBeforeInMinutes() != null) {
                Map<String, Integer> timeNotifyBefore =
                        dateTimeService.subtractMinutes(hours, minutes, requestDto.getNotifyBeforeInMinutes());
                workoutReminderDay.setHoursNotifyBefore(timeNotifyBefore.get("hours"));
                workoutReminderDay.setMinutesNotifyBefore(timeNotifyBefore.get("minutes"));
            }

            workoutReminderDay.setWorkoutReminder(workoutReminderSaved);
            WorkoutReminderDay workoutReminderDaySaved = workoutReminderDayRepository.save(workoutReminderDay);
            workoutReminderSaved.addReminderDays(List.of(workoutReminderDaySaved));
        }

        if (workoutReminderSaved.getReminderType().equals(ReminderType.DAYS)) {

            int hours = requestDto.getDays().get(0).getHours();
            int minutes = requestDto.getDays().get(0).getMinutes();

            Map<String, Integer> timeNotifyBeforeDefault =
                    dateTimeService.subtractMinutes(hours, minutes, NOTIFY_BEFORE_IN_MINUTES_DEFAULT);

            List<WorkoutReminderDay> days = new ArrayList<>();
            for (DayRequestDto dayRequestDto : requestDto.getDays()) {
                WorkoutReminderDay workoutReminderDay = WorkoutReminderDay.builder()
                        .day(dayRequestDto.getDay())
                        .hours(dayRequestDto.getHours())
                        .minutes(dayRequestDto.getMinutes())
                        .hoursNotifyBeforeDefault(timeNotifyBeforeDefault.get("hours"))
                        .minutesNotifyBeforeDefault(timeNotifyBeforeDefault.get("minutes"))
                        .isActive(true)
                        .createdAt(dateTimeService.getCurrentDate())
                        .build();

                if (requestDto.getNotifyBeforeInMinutes() != null) {
                    Map<String, Integer> timeNotifyBefore =
                            dateTimeService.subtractMinutes(hours, minutes, requestDto.getNotifyBeforeInMinutes());
                    workoutReminderDay.setHoursNotifyBefore(timeNotifyBefore.get("hours"));
                    workoutReminderDay.setMinutesNotifyBefore(timeNotifyBefore.get("minutes"));
                }

                workoutReminderDay.setWorkoutReminder(workoutReminderSaved);
                WorkoutReminderDay workoutReminderDaySaved = workoutReminderDayRepository.save(workoutReminderDay);
                days.add(workoutReminderDaySaved);
            }
            workoutReminderSaved.addReminderDays(days);
        }
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
            responseDto.setDays(new ArrayList<>());
            workoutReminder.getDaysSortedById().forEach(day -> {
                DayResponseDto dayResponseDto = modelMapper.map(day, DayResponseDto.class);
                responseDto.getDays().add(dayResponseDto);
            });
            return responseDto;
        });

        return responseDtoPage;
    }

    @Override
    public void updateScheduler(WorkoutReminder workoutReminder) {

        List<WorkoutReminderDay> days = workoutReminder.getDaysSortedById();
        TimeZone userTimeZone = TimeZone.getTimeZone("Europe/Kyiv");

        for (WorkoutReminderDay day : days) {

            boolean isTimeInBetweenSchedulers_activityTime = dateTimeService.isTimeInBetweenSchedulers(
                    day.getDay(), day.getHours(), day.getMinutes(), userTimeZone);
            if (isTimeInBetweenSchedulers_activityTime) {
                updateSchedulerHelper(
                        workoutReminder,
                        NotificationType.MAIN,
                        day.getHours(),
                        day.getMinutes(),
                        day.getHours(),
                        day.getMinutes(),
                        userTimeZone);
            }

            boolean isTimeInBetweenSchedulers_notifyBeforeTime = dateTimeService.isTimeInBetweenSchedulers(
                    day.getDay(), day.getHoursNotifyBefore(), day.getMinutesNotifyBefore(), userTimeZone);
            if (isTimeInBetweenSchedulers_notifyBeforeTime) {
                updateSchedulerHelper(
                        workoutReminder,
                        NotificationType.BEFORE,
                        day.getHoursNotifyBefore(),
                        day.getMinutesNotifyBefore(),
                        day.getHours(),
                        day.getMinutes(),
                        userTimeZone);
            }

            boolean isTimeInBetweenSchedulers_notifyBeforeDefaultTime = dateTimeService.isTimeInBetweenSchedulers(
                    day.getDay(), day.getHoursNotifyBeforeDefault(), day.getMinutesNotifyBeforeDefault(), userTimeZone);
            if (isTimeInBetweenSchedulers_notifyBeforeDefaultTime) {
                updateSchedulerHelper(
                        workoutReminder,
                        NotificationType.DEFAULT,
                        day.getHoursNotifyBeforeDefault(),
                        day.getMinutesNotifyBeforeDefault(),
                        day.getHours(),
                        day.getMinutes(),
                        userTimeZone);
            }
        }
    }

    private void updateSchedulerHelper(
            WorkoutReminder workoutReminder,
            NotificationType notificationType,
            int notificationHour,
            int notificationMinutes,
            int activityHour,
            int activityMinutes,
            TimeZone userTimeZone) {

        LocalDateTime notificationStartDateTime = LocalDateTime.of(
                dateTimeService.getCurrentYear(),
                dateTimeService.getCurrentMonth(),
                dateTimeService.getCurrentDayOfMonth(),
                notificationHour,
                notificationMinutes);
        ZonedDateTime notificationStartDateTimeInUserZone =
                ZonedDateTime.of(notificationStartDateTime, userTimeZone.toZoneId());
        ZonedDateTime notificationStartDateTimeInServerZone = dateTimeService.convertToNewZone(
                notificationStartDateTimeInUserZone, dateTimeService.getServerTimezone());

        LocalDateTime activityStartDateTime = LocalDateTime.of(
                dateTimeService.getCurrentYear(),
                dateTimeService.getCurrentMonth(),
                dateTimeService.getCurrentDayOfMonth(),
                activityHour,
                activityMinutes);
        ZonedDateTime activityStartDateTimeInUserZone =
                ZonedDateTime.of(activityStartDateTime, userTimeZone.toZoneId());

        TaskDto taskDto = TaskDto.builder()
                .userId(workoutReminder.getUser().getId())
                .notificationType(notificationType)
                .notifyBeforeInMinutes(workoutReminder.getNotifyBeforeInMinutes())
                .activityId(workoutReminder.getWorkout().getId())
                .activityType(ActivityType.WORKOUT)
                .reminderId(workoutReminder.getId())
                .notificationStartDateTimeInServerZone(notificationStartDateTimeInServerZone)
                .activityStartDateTimeInUserZone(activityStartDateTimeInUserZone)
                .build();

        notificationService.addScheduledFuture(taskDto);
    }
}
