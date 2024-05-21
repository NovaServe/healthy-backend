package healthy.lifestyle.backend.reminder.workout.repository;

import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminder;
import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminderDayId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WorkoutDayIdRepository extends JpaRepository<WorkoutReminderDayId, Long> {}
