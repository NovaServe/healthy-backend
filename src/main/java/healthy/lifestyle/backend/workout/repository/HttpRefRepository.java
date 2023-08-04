package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.HttpRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HttpRefRepository extends JpaRepository<HttpRef, Long> {
    boolean existsById(long id);
}
