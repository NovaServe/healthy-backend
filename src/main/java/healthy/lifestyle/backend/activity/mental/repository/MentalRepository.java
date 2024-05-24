package healthy.lifestyle.backend.activity.mental.repository;

import healthy.lifestyle.backend.activity.mental.model.Mental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MentalRepository extends JpaRepository<Mental, Long> {
    @Query("SELECT DISTINCT m FROM Mental m WHERE m.isCustom = false OR (m.isCustom = true AND m.user.id = :userId) ")
    Page<Mental> findDefaultAndCustomMentals(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM Mental m WHERE (m.title = :title AND m.isCustom = true AND m.user.id = :userId)"
            + "OR (m.title = :title AND m.isCustom = false)")
    List<Mental> getDefaultAndCustomMentalByTitleAndUserId(String title, long userId);

    @Query("SELECT m FROM Mental m WHERE m.user.id = :userId AND m.id = :mentalId AND m.isCustom = true")
    Optional<Mental> findCustomByMentalIdAndUserId(long mentalId, long userId);

    @Query("SELECT DISTINCT m FROM Mental m WHERE (:userId IS NULL OR m.user.id = :userId) AND m.isCustom = :isCustom "
            + "AND (:title IS NULL OR m.title ILIKE %:title%) "
            + "AND (:description IS NULL OR m.description ILIKE %:description%) "
            + "AND (:type IS NULL OR m.type = :mental_type_id)")
    Page<Mental> findDefaultOrCustomWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("type") Long mentalTypeId,
            Pageable pageable);

    @Query("SELECT DISTINCT m FROM Mental m WHERE (m.isCustom = false OR (m.isCustom = true AND m.user.id = :userId)) "
            + "AND (:title IS NULL OR m.title ILIKE %:title%) "
            + "AND (:description IS NULL OR m.description ILIKE %:description%) "
            + "AND (:type IS NULL OR m.type = :mental_type_id)")
    Page<Mental> findDefaultAndCustomWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("type") Long mentalTypeId,
            Pageable pageable);
}
