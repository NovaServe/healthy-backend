package healthy.lifestyle.backend.workout.model;

import healthy.lifestyle.backend.exception.ExceptionGeneric;
import healthy.lifestyle.backend.users.model.User;
import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "http_refs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HttpRef implements ExceptionGeneric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = false, nullable = false)
    private String name;

    @Column(name = "ref", unique = false, nullable = false)
    private String ref;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    @ManyToMany(mappedBy = "httpRefs")
    @OrderBy("id")
    private Set<Exercise> exercises;

    // FOREIGN KEY(user_id) REFERENCES users(id)
    // Only for isCustom=true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
