package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.Exercise;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e JOIN e.users u WHERE u.id = :userId AND e.title = :title AND e.isCustom = true")
    Optional<Exercise> findByTitleAndUserId(String title, Long userId);
}
