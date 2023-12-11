package healthy.lifestyle.backend.users.repository;

import healthy.lifestyle.backend.users.model.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByName(String name);
}
