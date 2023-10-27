package healthy.lifestyle.backend.users.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CountryResponseDto {
    private Long id;
    private String name;
}
