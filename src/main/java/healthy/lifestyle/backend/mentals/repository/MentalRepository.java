package healthy.lifestyle.backend.mentals.repository;

import healthy.lifestyle.backend.mentals.model.Mental;
import healthy.lifestyle.backend.mentals.model.MentalType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentalRepository extends JpaRepository<Mental, Long> {
    @Query("SELECT DISTINCT m FROM Mental m WHERE (:type IS NULL OR m.type = :type) "
            + "AND (:userId IS NULL OR m.user.id = :userId) "
            + "AND m.isCustom = :isCustom "
            + "AND (:title IS NULL OR m.title ILIKE %:title%) "
            + "AND (:description IS NULL OR m.description ILIKE %:description%) ")
    Page<Mental> findDefaultOrCustomWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("type") MentalType type,
            Pageable pageable);

    @Query("SELECT DISTINCT m FROM Mental m WHERE (:type IS NULL OR m.type = :type) "
            + "AND (m.isCustom = false OR (m.isCustom = true AND m.user.id = :userId)) "
            + "AND (:title IS NULL OR m.title ILIKE %:title%) "
            + "AND (:description IS NULL OR m.description ILIKE %:description%) ")
    Page<Mental> findDefaultAndCustomWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("type") MentalType type,
            Pageable pageable);

    @Query("SELECT m FROM Mental m WHERE m.isCustom = false")
    List<Mental> findAllDefault(Sort sort);

    @Query("SELECT m FROM Mental m WHERE m.user.id = :userId AND m.isCustom = true")
    List<Mental> findCustomMentalByUserId(long userId, Sort sort);

    @Query("SELECT m FROM Mental m WHERE m.user.id = :userId AND m.title = :title AND m.isCustom = true")
    List<Mental> findCustomMentalByTitleAndUserId(String title, Long userId);

    @Query("SELECT m FROM Mental m WHERE m.user.id = :userId AND m.id = :mentalId AND m.isCustom = true")
    List<Mental> findCustomByMentalIdAndUserId(long mentalId, long userId);
}
