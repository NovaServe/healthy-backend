package healthy.lifestyle.backend.nutrition.model;

import healthy.lifestyle.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * nutritions table preserves both default and custom nutritions.
 * If nutrition is default then is_custom is false, otherwise is true.
 */
@Entity
@Table(name = "nutritions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Nutrition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = false)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = true)
    private boolean isCustom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_type_id")
    private NutritionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
