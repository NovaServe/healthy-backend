package healthy.lifestyle.backend.reminder.workout.repository;

import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkoutReminderRepository extends JpaRepository<WorkoutReminder, Long> {
    @Query("SELECT wr FROM WorkoutReminder wr WHERE wr.user.id = :userId AND wr.workout.id = :workoutId")
    Optional<WorkoutReminder> findByUserIdAndWorkoutId(long userId, long workoutId);

    @Query(
            "SELECT wr FROM WorkoutReminder wr WHERE wr.user.id = :userId AND (:isActive IS NULL OR wr.isActive = :isActive)")
    Page<WorkoutReminder> findByUserId(long userId, Boolean isActive, Pageable pageable);

    @Query("SELECT wr FROM WorkoutReminder wr WHERE wr.user.id = :userId")
    List<WorkoutReminder> findByUserId(long userId);
}
