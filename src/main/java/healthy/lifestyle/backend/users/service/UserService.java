package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.LoginResponseDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Exercise;

public interface UserService {
    SignupResponseDto createUser(SignupRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto requestDto);

    void addExercise(long userId, Exercise exercise);

    User getUserById(long userId);
}
