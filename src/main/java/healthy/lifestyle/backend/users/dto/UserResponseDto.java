package healthy.lifestyle.backend.users.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserResponseDto {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private Long countryId;

    private Integer age;
}
