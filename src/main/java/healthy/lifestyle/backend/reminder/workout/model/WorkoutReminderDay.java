package healthy.lifestyle.backend.reminder.workout.model;

import healthy.lifestyle.backend.calendar.model.ReminderDayBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workout_reminder_days")
public class WorkoutReminderDay extends ReminderDayBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_reminder_id", nullable = false, unique = false)
    private WorkoutReminder workoutReminder;
}
