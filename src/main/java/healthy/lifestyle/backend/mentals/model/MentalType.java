package healthy.lifestyle.backend.mentals.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "mental_types")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MentalType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "type")
    private Set<Mental> mentals;
}
