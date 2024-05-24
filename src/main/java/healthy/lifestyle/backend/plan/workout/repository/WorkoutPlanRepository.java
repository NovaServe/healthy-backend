package healthy.lifestyle.backend.plan.workout.repository;

import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    @Query("SELECT wr FROM WorkoutPlan wr WHERE wr.user.id = :userId AND wr.workout.id = :workoutId")
    List<WorkoutPlan> findByUserIdAndWorkoutId(long userId, long workoutId);

    @Query(
            "SELECT wr FROM WorkoutPlan wr WHERE wr.user.id = :userId AND (:isActive IS NULL OR wr.isActive = :isActive)")
    Page<WorkoutPlan> findByUserId(long userId, Boolean isActive, Pageable pageable);

    @Query("SELECT wr FROM WorkoutPlan wr WHERE wr.user.id = :userId")
    List<WorkoutPlan> findByUserId(long userId);
}
