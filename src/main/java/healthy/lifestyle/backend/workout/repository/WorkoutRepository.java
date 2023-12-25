package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.Workout;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    @Query("SELECT w FROM Workout w WHERE w.isCustom = false")
    List<Workout> findAllDefault(Sort sort);

    @Query("SELECT w FROM Workout w WHERE w.user.id = :userId AND w.title = :title AND w.isCustom = true")
    List<Workout> findCustomByTitleAndUserId(String title, Long userId);

    @Query("SELECT w FROM Workout w WHERE w.user.id = :userId AND w.id = :workoutId AND w.isCustom = true")
    List<Workout> findCustomByWorkoutIdAndUserId(long workoutId, long userId);
}
