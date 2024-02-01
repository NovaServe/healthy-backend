package healthy.lifestyle.backend.admin.user.repository;

import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAdminRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role) AND "
            + "(:username IS NULL OR u.username = :username) AND "
            + "(:email IS NULL OR u.email = :email) AND "
            + "(:fullName IS NULL OR u.fullName = :fullName) AND "
            + "(:country IS NULL OR u.country = :country) AND "
            + "(:age IS NULL OR u.age = :age)")
    List<User> findWithFilter(
            @Param("role") Role role,
            @Param("username") String username,
            @Param("email") String email,
            @Param("fullName") String fullName,
            @Param("country") Country country,
            @Param("age") Integer age);
}
