package healthy.lifestyle.backend.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;

    private String username;

    private String email;

    private String fullName;

    private Long countryId;

    private Integer age;
}
