package healthy.lifestyle.backend.activity.workout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRefResponseDto {
    private long id;

    private String name;

    private String description;

    private String ref;

    private String httpRefTypeName;

    @JsonProperty(value = "isCustom")
    private boolean isCustom;
}
