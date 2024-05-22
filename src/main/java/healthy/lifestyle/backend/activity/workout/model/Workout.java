package healthy.lifestyle.backend.activity.workout.model;

import healthy.lifestyle.backend.plan.workout.model.WorkoutCompletionRecord;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 * workouts table preserves both default and custom workouts.
 * If workout is default then is_custom is false, otherwise is true.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    // user_id column preserves the user id value only for custom workouts (when workout.isCustom is true).
    // If the workout is default (when workout.isCustom is false), then user_id is null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "workouts_exercises",
            joinColumns = @JoinColumn(name = "workout_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id", referencedColumnName = "id"))
    private Set<Exercise> exercises;

    @OneToMany(mappedBy = "workout")
    private Set<WorkoutPlan> workoutPlans;

    @OneToMany(mappedBy = "workout")
    private Set<WorkoutCompletionRecord> workoutCompletionRecords;

    @OneToMany(mappedBy = "workout")
    private Set<WorkoutExercises> workoutExercises;

    public List<Exercise> getExercisesSortedById() {
        return this.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
    }

    public List<Long> getSortedExercisesIds() {
        return this.getExercisesSortedById().stream().map(Exercise::getId).toList();
    }

    public List<BodyPart> getDistinctBodyPartsSortedById() {
        Set<BodyPart> bodyParts = new HashSet<>();
        this.getExercises().forEach(exercise -> bodyParts.addAll(exercise.getBodyParts()));
        return bodyParts.stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
    }
}
