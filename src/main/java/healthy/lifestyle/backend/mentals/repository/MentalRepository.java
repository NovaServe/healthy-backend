package healthy.lifestyle.backend.mentals.repository;

import healthy.lifestyle.backend.mentals.model.Mental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentalRepository extends JpaRepository<Mental, Long> {}
