package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.*;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Exercise;

public interface UserService {
    SignupResponseDto createUser(SignupRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto requestDto);

    void addExercise(long userId, Exercise exercise);

    User getUserById(long userId);

    UserResponseDto updateUser(Long userId, UpdateUserRequestDto requestDto);

    void deleteUser(Long userId);
}
