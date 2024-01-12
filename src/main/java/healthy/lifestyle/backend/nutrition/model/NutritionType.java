package healthy.lifestyle.backend.nutrition.model;

import jakarta.persistence.*;
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
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "name", unique = false, nullable = false)
    private String name;

    @Column(name = "supplement", unique = false, nullable = false)
    private String supplement;

    @Column(name = "recipe", unique = false, nullable = false)
    private String recipe;
}
