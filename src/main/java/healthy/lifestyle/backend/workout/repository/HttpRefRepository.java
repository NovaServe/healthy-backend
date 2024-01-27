package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HttpRefRepository extends JpaRepository<HttpRef, Long> {
    boolean existsById(long id);

    @Query("SELECT h FROM HttpRef h WHERE h.isCustom = false")
    List<HttpRef> findAllDefault(Sort sort);

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

    @Query("SELECT h FROM HttpRef h WHERE h.user.id = :userId AND h.isCustom = true")
    List<HttpRef> findCustomByUserId(long userId, Sort sort);

    @Query("SELECT h FROM HttpRef h WHERE h.user.id = :userId AND h.name = :name AND h.isCustom = true")
    Optional<HttpRef> findCustomByNameAndUserId(String name, Long userId);
}
