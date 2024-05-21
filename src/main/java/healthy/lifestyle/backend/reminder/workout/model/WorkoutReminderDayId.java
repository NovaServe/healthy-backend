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
@Table(name = "json_ids")
public class WorkoutReminderDayId{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "json_id", nullable = false, unique = false)
    private Long json_id;
}
