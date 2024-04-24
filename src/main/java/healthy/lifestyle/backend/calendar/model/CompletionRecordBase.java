package healthy.lifestyle.backend.calendar.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class CompletionRecordBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "completed_at", nullable = false, unique = false)
    private LocalDate completedAt;
}
