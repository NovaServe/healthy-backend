package healthy.lifestyle.backend.mentals.repository;

import healthy.lifestyle.backend.mentals.model.MentalType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentalTypeRepository extends JpaRepository<MentalType, Long> {

    Optional<MentalType> findByName(String name);
}
