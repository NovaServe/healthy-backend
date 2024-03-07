package healthy.lifestyle.backend.activity.workout.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workout_exercises")
public class WorkoutExercises {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false, unique = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = true, unique = false)
    private Exercise exercise;

    @Column(name = "is_active", nullable = false, unique = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDate createdAt;

    @Column(name = "deactivated_at", nullable = true, unique = false)
    private LocalDate deactivatedAt;
}
