package healthy.lifestyle.backend.plan.workout.repository;

import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import java.util.List;
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

    @Query(
            value = "SELECT DISTINCT w.id, w.title FROM workouts w LEFT JOIN workout_plans wp "
                    + "ON w.id = wp.workout_id AND wp.user_id = :userId "
                    + "WHERE wp.id IS NULL AND w.is_custom = false;",
            nativeQuery = true)
    List<Object[]> getDefaultWorkoutsWithoutPlans(long userId);

    @Query(
            value = "SELECT DISTINCT w.id, w.title FROM workouts w LEFT JOIN workout_plans wp "
                    + "ON w.id = wp.workout_id AND w.user_id = :userId AND wp.user_id = :userId "
                    + "WHERE wp.workout_id IS NULL AND w.is_custom = true;",
            nativeQuery = true)
    List<Object[]> getCustomWorkoutsWithoutPlans(long userId);

    @Query(value = "SELECT wp FROM WorkoutPlan wp WHERE wp.isActive = true AND wp.user.id = :userId")
    List<WorkoutPlan> getAcitveWorkoutPlans(long userId);
}
