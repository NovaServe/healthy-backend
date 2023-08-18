package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.LoginResponseDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;

public interface UserService {
    SignupResponseDto createUser(SignupRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto requestDto);
}
