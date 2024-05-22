package healthy.lifestyle.backend.activity.workout.model;

import healthy.lifestyle.backend.plan.workout.model.WorkoutCompletionRecord;
import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 * exercises table preserves both default and custom exercises.
 * If exercise is default then is_custom is false, otherwise is true.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "needs_equipment", unique = false, nullable = false)
    private boolean needsEquipment;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    // user_id column preserves the user id value only for custom exercises (when exercise.isCustom is true).
    // If the exercise is default (when exercise.isCustom is false), then user_id is null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exercises_body_parts",
            joinColumns = @JoinColumn(name = "exercise_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "body_part_id", referencedColumnName = "id"))
    private Set<BodyPart> bodyParts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exercises_http_refs",
            joinColumns = @JoinColumn(name = "exercise_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "http_ref_id", referencedColumnName = "id"))
    private Set<HttpRef> httpRefs;

    @OneToMany(mappedBy = "exercise")
    private Set<WorkoutExercises> workoutExercises;

    @OneToMany(mappedBy = "exercise")
    private Set<WorkoutCompletionRecord> workoutCompletionRecords;

    public List<BodyPart> getBodyPartsSortedById() {
        return this.getBodyParts().stream()
                .sorted(Comparator.comparing(BodyPart::getId))
                .toList();
    }

    public List<Long> getBodyPartsIdsSorted() {
        return this.getBodyPartsSortedById().stream().map(BodyPart::getId).toList();
    }

    public List<HttpRef> getHttpRefsSortedById() {
        return this.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
    }

    public List<Long> getHttpRefsIdsSorted() {
        return this.getHttpRefsSortedById().stream().map(HttpRef::getId).toList();
    }

    public void addBodyPart(BodyPart bodyPart) {
        if (this.getBodyParts() == null) {
            this.setBodyParts(new HashSet<>());
        }
        this.getBodyParts().add(bodyPart);
    }
}
