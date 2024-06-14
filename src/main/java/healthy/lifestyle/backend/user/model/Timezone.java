package healthy.lifestyle.backend.user.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timezones")
public class Timezone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "gmt", nullable = false, unique = false)
    private String GMT;

    @OneToMany(mappedBy = "timezone")
    private Set<User> users;
}
