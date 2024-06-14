package healthy.lifestyle.backend.user.repository;

import healthy.lifestyle.backend.user.model.Timezone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimezoneRepository extends JpaRepository<Timezone, Long> {}
