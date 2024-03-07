package healthy.lifestyle.backend.calendar.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class ReminderDayBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "day", nullable = true, unique = false)
    private DayOfWeek day;

    // [0; 23]
    @Column(name = "hours", nullable = false, unique = false)
    private int hours;

    // [0; 59]
    @Column(name = "minutes", nullable = false, unique = false)
    private int minutes;

    @Column(name = "hours_notify_before", nullable = true, unique = false)
    private int hoursNotifyBefore;

    @Column(name = "minutes_notify_before", nullable = true, unique = false)
    private int minutesNotifyBefore;

    @Column(name = "hours_notify_before_default", nullable = false, unique = false)
    private int hoursNotifyBeforeDefault;

    @Column(name = "minutes_notify_before_default", nullable = false, unique = false)
    private int minutesNotifyBeforeDefault;

    @Column(name = "is_active", nullable = false, unique = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDate createdAt;

    @Column(name = "deactivated_at", nullable = true, unique = false)
    private LocalDate deactivatedAt;
}
