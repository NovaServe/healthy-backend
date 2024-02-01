package healthy.lifestyle.backend.mental.repository;

import healthy.lifestyle.backend.mental.model.Mental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentalRepository extends JpaRepository<Mental, Long> {}
