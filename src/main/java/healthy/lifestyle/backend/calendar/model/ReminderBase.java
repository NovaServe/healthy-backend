package healthy.lifestyle.backend.calendar.model;

import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class ReminderBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_active", nullable = false, unique = false)
    private Boolean isActive;

    // Whether a reminder is paused by a user.
    @Column(name = "is_paused", nullable = false, unique = false)
    private boolean isPaused;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDate createdAt;
}
