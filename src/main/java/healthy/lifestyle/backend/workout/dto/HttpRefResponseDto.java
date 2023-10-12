package healthy.lifestyle.backend.workout.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HttpRefResponseDto {
    private long id;
    private String name;
    private String description;
    private String ref;
    private boolean isCustom;
}
