package healthy.lifestyle.backend.users.repository;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " + "(:username IS NULL OR u.username = :username) AND "
            + "(:email IS NULL OR u.email = :email) AND "
            + "(:fullName IS NULL OR u.fullName = :fullName) AND "
            + "(:country IS NULL OR u.country = :country) AND"
            + "(:age IS NULL OR u.age = :age)")
    List<User> findByFilters(
            @Param("username") String username,
            @Param("email") String email,
            @Param("fullName") String fullName,
            @Param("country") Country country,
            @Param("age") Integer age);
}
