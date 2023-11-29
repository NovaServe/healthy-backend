package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.Exercise;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e JOIN e.users u WHERE u.id = :userId AND e.title = :title AND e.isCustom = true")
    Optional<Exercise> findCustomByTitleAndUserId(String title, Long userId);

    @Query("SELECT e FROM Exercise e WHERE e.isCustom = false")
    List<Exercise> findAllDefault(Sort sort);

    @Query("SELECT e FROM Exercise e JOIN e.users u WHERE u.id = :userId AND e.isCustom = true")
    List<Exercise> findCustomByUserId(long userId, Sort sort);

    @Query("SELECT e FROM Exercise e WHERE e.id = :exerciseId AND e.isCustom = false")
    Exercise findDefaultById(long exerciseId);

    @Query("SELECT e FROM Exercise e JOIN e.users u WHERE u.id = :userId AND e.id = :exerciseId AND e.isCustom = true")
    Optional<Exercise> findCustomByExerciseIdAndUserId(long exerciseId, long userId);
}
