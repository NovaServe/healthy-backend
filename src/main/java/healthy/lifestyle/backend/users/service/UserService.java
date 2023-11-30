package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.*;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;

public interface UserService {
    SignupResponseDto createUser(SignupRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto requestDto);

    void addExercise(long userId, Exercise exercise);

    void addWorkout(User user, Workout workout);

    User getUserById(long userId);

    void removeWorkout(User user, Workout workout);

    UserResponseDto updateUser(Long userId, UpdateUserRequestDto requestDto);

    long deleteUser(Long userId);

    UserResponseDto getUserDetailsById(long userId);

    void deleteUserExercise(long userId, Exercise exercise);
}
