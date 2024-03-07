package healthy.lifestyle.backend.activity.workout.repository;

import healthy.lifestyle.backend.activity.workout.model.Workout;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    @Query("SELECT w FROM Workout w WHERE w.user.id = :userId AND w.title = :title AND w.isCustom = true")
    List<Workout> findCustomByTitleAndUserId(String title, Long userId);

    @Query("SELECT w FROM Workout w WHERE (w.title = :title AND w.isCustom = true AND w.user.id = :userId) "
            + "OR (w.title = :title AND w.isCustom = false)")
    List<Workout> findDefaultAndCustomByTitleAndUserId(String title, Long userId);

    @Query("SELECT DISTINCT w FROM Workout w JOIN w.exercises.bodyParts bp "
            + "JOIN w.exercises we WHERE element(bp).id IN :bodyPartsIds AND element(we).needsEquipment = :needsEquipment "
            + "AND (:userId IS NULL OR w.user.id = :userId) AND w.isCustom = :isCustom "
            + "AND (:title IS NULL OR w.title ILIKE %:title%) "
            + "AND (:description IS NULL OR w.description ILIKE %:description%)")
    Page<Workout> findDefaultOrCustomNeedsEquipmentWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("needsEquipment") Boolean needsEquipment,
            @Param("bodyPartsIds") List<Long> bodyPartsIds,
            Pageable pageable);

    @Query("SELECT DISTINCT w FROM Workout w JOIN w.exercises.bodyParts bp WHERE element(bp).id IN :bodyPartsIds "
            + "AND (:userId IS NULL OR w.user.id = :userId) AND w.isCustom = :isCustom "
            + "AND (:title IS NULL OR w.title ILIKE %:title%) "
            + "AND (:description IS NULL OR w.description ILIKE %:description%)")
    Page<Workout> findDefaultOrCustomWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("bodyPartsIds") List<Long> bodyPartsIds,
            Pageable pageable);

    @Query("SELECT DISTINCT w FROM Workout w JOIN w.exercises.bodyParts bp "
            + "JOIN w.exercises we WHERE element(bp).id IN :bodyPartsIds AND element(we).needsEquipment = :needsEquipment "
            + "AND (w.isCustom = false OR (w.isCustom = true AND w.user.id = :userId)) "
            + "AND (:title IS NULL OR w.title ILIKE %:title%) "
            + "AND (:description IS NULL OR w.description ILIKE %:description%)")
    Page<Workout> findDefaultAndCustomNeedsEquipmentWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("needsEquipment") Boolean needsEquipment,
            @Param("bodyPartsIds") List<Long> bodyPartsIds,
            Pageable pageable);

    @Query("SELECT DISTINCT w FROM Workout w JOIN w.exercises.bodyParts bp WHERE element(bp).id IN :bodyPartsIds "
            + "AND (w.isCustom = false OR (w.isCustom = true AND w.user.id = :userId)) "
            + "AND (:title IS NULL OR w.title ILIKE %:title%) "
            + "AND (:description IS NULL OR w.description ILIKE %:description%)")
    Page<Workout> findDefaultAndCustomWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("bodyPartsIds") List<Long> bodyPartsIds,
            Pageable pageable);
}
