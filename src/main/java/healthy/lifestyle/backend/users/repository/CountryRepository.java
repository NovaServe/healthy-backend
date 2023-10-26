package healthy.lifestyle.backend.users.repository;

import healthy.lifestyle.backend.users.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {}
