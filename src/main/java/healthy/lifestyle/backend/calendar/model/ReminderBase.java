package healthy.lifestyle.backend.calendar.model;

import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, unique = false)
    private ReminderType reminderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false, unique = false)
    private LocalDate startDate;

    // Either endDate or applyDays should be specified.
    // If occasionally both fields are specified,
    // then endDate has a priority, and applyDays is ignored.
    @Column(name = "end_date", nullable = true, unique = false)
    private LocalDate endDate;

    @Column(name = "apply_days", nullable = true, unique = false)
    private Integer applyDays;

    // Whether a reminder is deleted by a user.
    @Column(name = "is_active", nullable = false, unique = false)
    private boolean isActive;

    // Whether a reminder is paused by a user.
    @Column(name = "is_paused", nullable = false, unique = false)
    private boolean isPaused;

    @Column(name = "pause_start_date", nullable = true, unique = false)
    private LocalDate pauseStartDate;

    @Column(name = "pause_end_date", nullable = true, unique = false)
    private LocalDate pauseEndDate;

    @Column(name = "is_paused_billing", nullable = false, unique = false)
    private boolean isPausedBilling;

    @Column(name = "notify_before_in_minutes", nullable = true, unique = false)
    private Integer notifyBeforeInMinutes;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDate createdAt;
}
