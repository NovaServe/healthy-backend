package healthy.lifestyle.backend.activity.mental.model;

import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 * mental_workouts table preserves both default and custom workouts.
 * If mental_workout is default then is_custom is false, otherwise is true.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mental_workouts")
public class MentalWorkout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mental_workout_activities",
            joinColumns = @JoinColumn(name = "mental_workout_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "mental_activity_id", referencedColumnName = "id"))
    private Set<MentalActivity> mentalActivities;

    public List<MentalActivity> getMentalActivitiesSortedById() {
        return this.getMentalActivities().stream()
                .sorted(Comparator.comparingLong(MentalActivity::getId))
                .toList();
    }

    public List<Long> getSortedMentalActivitiesIds() {
        return this.getMentalActivitiesSortedById().stream()
                .map(MentalActivity::getId)
                .toList();
    }
}
