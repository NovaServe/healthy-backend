package healthy.lifestyle.backend.activity.workout.repository;

import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyPartRepository extends JpaRepository<BodyPart, Long> {
    boolean existsById(long id);
}
