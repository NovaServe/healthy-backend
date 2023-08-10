package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;

public interface UserService {
    SignupResponseDto createUser(SignupRequestDto requestDto);
}
