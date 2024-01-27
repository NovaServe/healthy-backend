package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.Exercise;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e WHERE e.user.id = :userId AND e.title = :title AND e.isCustom = true")
    Optional<Exercise> findCustomByTitleAndUserId(String title, Long userId);

    @Query("SELECT DISTINCT e FROM Exercise e JOIN e.bodyParts bp WHERE element(bp).id IN :bodyPartsIds "
            + "AND (:userId IS NULL OR e.user.id = :userId) AND e.isCustom = :isCustom "
            + "AND (:title IS NULL OR e.title ILIKE %:title%) "
            + "AND (:description IS NULL OR e.description ILIKE %:description%) "
            + "AND (:needsEquipment IS NULL OR e.needsEquipment = :needsEquipment)")
    Page<Exercise> findDefaultOrCustomWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("needsEquipment") Boolean needsEquipment,
            @Param("bodyPartsIds") List<Long> bodyPartsIds,
            Pageable pageable);

    @Query("SELECT DISTINCT e FROM Exercise e JOIN e.bodyParts bp WHERE element(bp).id IN :bodyPartsIds "
            + "AND (e.isCustom = false OR (e.isCustom = true AND e.user.id = :userId)) "
            + "AND (:title IS NULL OR e.title ILIKE %:title%) "
            + "AND (:description IS NULL OR e.description ILIKE %:description%) "
            + "AND (:needsEquipment IS NULL OR e.needsEquipment = :needsEquipment)")
    Page<Exercise> findDefaultAndCustomWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("needsEquipment") Boolean needsEquipment,
            @Param("bodyPartsIds") List<Long> bodyPartsIds,
            Pageable pageable);

    @Query("SELECT e FROM Exercise e WHERE e.isCustom = false")
    List<Exercise> findAllDefault(Sort sort);

    @Query("SELECT e FROM Exercise e WHERE e.user.id = :userId AND e.isCustom = true")
    List<Exercise> findCustomByUserId(long userId, Sort sort);

    @Query("SELECT e FROM Exercise e WHERE e.id = :exerciseId AND e.isCustom = false")
    Exercise findDefaultById(long exerciseId);

    @Query("SELECT e FROM Exercise e WHERE e.user.id = :userId AND e.id = :exerciseId AND e.isCustom = true")
    Optional<Exercise> findCustomByExerciseIdAndUserId(long exerciseId, long userId);
}
