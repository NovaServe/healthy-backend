package healthy.lifestyle.backend.activity.mental.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mental_workout_activities")
public class MentalWorkoutActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mental_workout_id", nullable = false, unique = false)
    private MentalWorkout mentalWorkout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mental_activity_id", nullable = true, unique = false)
    private MentalActivity mentalActivity;

    @Column(name = "is_active", nullable = false, unique = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDate createdAt;

    @Column(name = "deactivated_at", nullable = true, unique = false)
    private LocalDate deactivatedAt;
}
