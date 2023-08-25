package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HttpRefRepository extends JpaRepository<HttpRef, Long> {
    boolean existsById(long id);

    @Query("SELECT h FROM HttpRef h WHERE h.isCustom = false")
    List<HttpRef> findAllDefault(Sort sort);

    @Query("SELECT h FROM HttpRef h JOIN h.exercises.users u WHERE u.id = :userId AND h.isCustom = true")
    List<HttpRef> findByUserId(long userId, Sort sort);
}
