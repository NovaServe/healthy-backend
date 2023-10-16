package healthy.lifestyle.backend.workout.model;

import healthy.lifestyle.backend.users.model.User;
import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "exercises")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    @Column(name = "needs_equipment", unique = false, nullable = false)
    private boolean needsEquipment;

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

    @ManyToMany(mappedBy = "exercises")
    private Set<User> users;
}
