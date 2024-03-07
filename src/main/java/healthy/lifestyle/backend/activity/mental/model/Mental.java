package healthy.lifestyle.backend.activity.mental.model;

import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.user.model.User;
import jakarta.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mentals")
public class Mental {
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
    private User user; // FOREIGN KEY(user_id) REFERENCES users(id)

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mentals_http_refs",
            joinColumns = @JoinColumn(name = "mental_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "http_ref_id", referencedColumnName = "id"))
    private Set<HttpRef> httpRefs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id") // FOREIGN KEY(type_id) REFERENCES mental_types(id)
    private MentalType type;

    public List<HttpRef> getHttpRefsSortedById() {
        return this.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
    }
}
