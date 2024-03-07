package healthy.lifestyle.backend.activity.mental.repository;

import healthy.lifestyle.backend.activity.mental.model.MentalType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentalTypeRepository extends JpaRepository<MentalType, Long> {
    Optional<MentalType> findByName(String name);
}
