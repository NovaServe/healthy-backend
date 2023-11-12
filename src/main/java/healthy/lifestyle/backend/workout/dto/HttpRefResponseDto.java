package healthy.lifestyle.backend.workout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(value = "isCustom")
    private boolean isCustom;
}
