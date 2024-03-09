package healthy.lifestyle.backend.notification.model;

import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "firebase_user_tokens")
public class FirebaseUserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_agent", unique = false, nullable = false)
    private String userAgent;

    @Column(name = "token", unique = false, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FOREIGN KEY(user_id) REFERENCES users(id)
    private User user;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDate createdAt;
}
