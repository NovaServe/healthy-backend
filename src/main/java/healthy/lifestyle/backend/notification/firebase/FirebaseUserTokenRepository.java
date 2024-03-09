package healthy.lifestyle.backend.notification.firebase;

import healthy.lifestyle.backend.notification.model.FirebaseUserToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirebaseUserTokenRepository extends JpaRepository<FirebaseUserToken, Long> {
    Optional<FirebaseUserToken> findByUser_IdAndToken(long userId, String token);

    List<FirebaseUserToken> findByUser_Id(long userId);
}
