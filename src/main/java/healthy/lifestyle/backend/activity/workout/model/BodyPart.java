package healthy.lifestyle.backend.activity.workout.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

/**
 * body_parts table preserves default values only.
 * Users cannot change values in this table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "body_parts")
public class BodyPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "bodyParts")
    @OrderBy("id")
    private Set<Exercise> exercises;
}
