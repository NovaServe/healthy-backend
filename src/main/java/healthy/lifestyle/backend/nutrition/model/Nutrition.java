package healthy.lifestyle.backend.nutrition.model;

import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.workout.model.HttpRef;
import jakarta.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 * nutritions table preserves both default and custom nutritions.
 * If nutrition is default then is_custom is false, otherwise is true.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nutritions")
public class Nutrition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "nutritions_http_refs",
            joinColumns = @JoinColumn(name = "nutrition_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "http_ref_id", referencedColumnName = "id"))
    private Set<HttpRef> httpRefs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_type_id")
    private NutritionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public List<HttpRef> getHttpRefsSortedById() {
        return this.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
    }
}
