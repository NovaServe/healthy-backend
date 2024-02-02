package healthy.lifestyle.backend.mental.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentalResponseDto {
    private Long id;

    private String title;

    private String description;

    @JsonProperty(value = "isCustom")
    private boolean isCustom;

    private List<HttpRefResponseDto> httpRefs;

    private Long mentalTypeId;
}
