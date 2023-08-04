package healthy.lifestyle.backend.users.repository;

import healthy.lifestyle.backend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
