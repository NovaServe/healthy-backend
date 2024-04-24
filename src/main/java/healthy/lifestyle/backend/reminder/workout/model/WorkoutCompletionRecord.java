package healthy.lifestyle.backend.reminder.workout.model;

import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.calendar.model.CompletionRecordBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workout_completion_records")
public class WorkoutCompletionRecord extends CompletionRecordBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false, unique = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = true, unique = false)
    private Exercise exercise;
}
