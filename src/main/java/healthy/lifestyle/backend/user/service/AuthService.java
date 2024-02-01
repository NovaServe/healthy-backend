package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.user.dto.LoginRequestDto;
import healthy.lifestyle.backend.user.dto.LoginResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto requestDto);
}
