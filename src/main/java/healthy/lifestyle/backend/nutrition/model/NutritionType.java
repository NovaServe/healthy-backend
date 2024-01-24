package healthy.lifestyle.backend.nutrition.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "nutrition_types")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NutritionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "type")
    private Set<Nutrition> nutritions;
}
