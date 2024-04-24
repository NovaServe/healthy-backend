package healthy.lifestyle.backend.admin.workout.repository;

import healthy.lifestyle.backend.activity.workout.model.Exercise;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseAdminRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e WHERE (:title is NULL OR e.title LIKE %:title%) AND "
            + "(:description is NULL OR e.description LIKE %:description%) AND "
            + "(:isCustom is NULL OR e.isCustom = :isCustom) AND "
            + "(:needsEquipment is NULL OR e.needsEquipment = :needsEquipment)")
    Optional<List<Exercise>> findWithFilter(
            @Param("title") String title,
            @Param("description") String description,
            @Param("isCustom") Boolean isCustom,
            @Param("needsEquipment") Boolean needsEquipment);
}
