package healthy.lifestyle.backend.workout.repository;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageJdbc<T> {
    private List<T> content;

    private int page;

    private int number;

    private int size;

    private int totalElements;

    private int totalPages;
}
