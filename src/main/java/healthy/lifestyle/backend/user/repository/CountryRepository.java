package healthy.lifestyle.backend.user.repository;

import healthy.lifestyle.backend.user.model.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByName(String name);
}
