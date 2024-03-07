package healthy.lifestyle.backend.reminder.workout.repository;

import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminderDay;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkoutReminderDayRepository extends JpaRepository<WorkoutReminderDay, Long> {
    @Query("SELECT wrd FROM WorkoutReminderDay wrd WHERE wrd.workoutReminder.id = :workoutId")
    List<WorkoutReminderDay> findByWorkoutReminderId(long workoutId);
}
