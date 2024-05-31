package healthy.lifestyle.backend.activity.workout.repository;

import healthy.lifestyle.backend.activity.workout.model.HttpRefType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HttpRefTypeRepository extends JpaRepository<HttpRefType, Long> {
    Optional<HttpRefType> findByName(String name);
}
