package healthy.lifestyle.backend.mentals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MentalResponseDto {
    private Long id;

    private String title;

    private String description;

    @JsonProperty(value = "isCustom")
    private boolean isCustom;

    private List<HttpRefResponseDto> httpRefs;

    private Long mentalId;
}
