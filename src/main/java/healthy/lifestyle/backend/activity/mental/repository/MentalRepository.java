package healthy.lifestyle.backend.activity.mental.repository;

import healthy.lifestyle.backend.activity.mental.model.Mental;
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
}
