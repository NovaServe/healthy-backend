package healthy.lifestyle.backend.activity.mental.repository;

import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MentalActivityRepository extends JpaRepository<MentalActivity, Long> {
    @Query(
            "SELECT DISTINCT m FROM MentalActivity m WHERE m.isCustom = false OR (m.isCustom = true AND m.user.id = :userId) ")
    Page<MentalActivity> findDefaultAndCustomMentalActivity(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM MentalActivity m WHERE (m.title = :title AND m.isCustom = true AND m.user.id = :userId)"
            + "OR (m.title = :title AND m.isCustom = false)")
    List<MentalActivity> getDefaultAndCustomMentalActivityByTitleAndUserId(String title, long userId);

    @Query("SELECT m FROM MentalActivity m WHERE m.user.id = :userId AND m.id = :mentalId AND m.isCustom = true")
    Optional<MentalActivity> findCustomByMentalIdAndUserId(long mentalId, long userId);

    @Query(
            "SELECT DISTINCT m FROM MentalActivity m WHERE (:userId IS NULL OR m.user.id = :userId) AND m.isCustom = :isCustom "
                    + "AND (:title IS NULL OR m.title ILIKE %:title%) "
                    + "AND (:description IS NULL OR m.description ILIKE %:description%) "
                    + "AND (:type IS NULL OR m.type.id = :type)")
    Page<MentalActivity> findDefaultOrCustomWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("type") Long mentalTypeId,
            Pageable pageable);

    @Query(
            "SELECT DISTINCT m FROM MentalActivity m WHERE (m.isCustom = false OR (m.isCustom = true AND m.user.id = :userId)) "
                    + "AND (:title IS NULL OR m.title ILIKE %:title%) "
                    + "AND (:description IS NULL OR m.description ILIKE %:description%) "
                    + "AND (:type IS NULL OR m.type.id = :type)")
    Page<MentalActivity> findDefaultAndCustomWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("type") Long mentalTypeId,
            Pageable pageable);
}
