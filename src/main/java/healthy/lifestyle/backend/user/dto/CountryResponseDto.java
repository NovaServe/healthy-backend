package healthy.lifestyle.backend.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryResponseDto {
    private Long id;

    private String name;
}
