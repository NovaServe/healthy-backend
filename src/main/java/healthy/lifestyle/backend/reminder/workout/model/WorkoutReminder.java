package healthy.lifestyle.backend.reminder.workout.model;

import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.calendar.model.ReminderBase;
import jakarta.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workout_reminders")
public class WorkoutReminder extends ReminderBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @OneToMany(mappedBy = "workoutReminder")
    private Set<WorkoutReminderDay> reminderDays;

    public List<WorkoutReminderDay> getDaysSortedById() {
        return reminderDays.stream()
                .sorted(Comparator.comparingLong(WorkoutReminderDay::getId))
                .toList();
    }

    public void addReminderDays(List<WorkoutReminderDay> days) {
        if (this.getReminderDays() == null) {
            this.setReminderDays(new HashSet<>());
        }
        this.getReminderDays().addAll(days);
    }
}
