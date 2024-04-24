package healthy.lifestyle.backend.activity.workout.repository;

import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HttpRefRepository extends JpaRepository<HttpRef, Long> {
    @Query("SELECT h FROM HttpRef h WHERE h.user.id = :userId AND h.name = :name AND h.isCustom = true")
    Optional<HttpRef> findCustomByNameAndUserId(String name, Long userId);

    @Query("SELECT h FROM HttpRef h WHERE (h.name = :name AND h.isCustom = true AND h.user.id = :userId) "
            + "OR (h.name = :name AND h.isCustom = false)")
    List<HttpRef> findDefaultAndCustomByNameAndUserId(String name, Long userId);

    @Query("SELECT h FROM HttpRef h WHERE (:userId IS NULL OR h.user.id = :userId) AND h.isCustom = :isCustom "
            + "AND (:name IS NULL OR h.name ILIKE %:name%) "
            + "AND (:description IS NULL OR h.description ILIKE %:description%)")
    Page<HttpRef> findDefaultOrCustomWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);

    @Query("SELECT h FROM HttpRef h WHERE (h.isCustom = false OR (h.isCustom = true AND h.user.id = :userId)) "
            + "AND (:name IS NULL OR h.name ILIKE %:name%) "
            + "AND (:description IS NULL OR h.description ILIKE %:description%)")
    Page<HttpRef> findDefaultAndCustomWithFilter(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);
}
