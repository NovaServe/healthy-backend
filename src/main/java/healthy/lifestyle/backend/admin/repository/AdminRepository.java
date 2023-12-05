package healthy.lifestyle.backend.admin.repository;

import healthy.lifestyle.backend.workout.model.Exercise;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e WHERE e.isCustom = true")
    List<Exercise> findAllCustomExercises(Sort sort);
}
