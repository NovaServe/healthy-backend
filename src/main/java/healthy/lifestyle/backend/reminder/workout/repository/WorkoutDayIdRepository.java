package healthy.lifestyle.backend.reminder.workout.repository;

import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminderDayId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutDayIdRepository extends JpaRepository<WorkoutReminderDayId, Long> {}
