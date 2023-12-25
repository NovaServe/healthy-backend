package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.*;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;

public interface UserService {
    void createUser(SignupRequestDto requestDto);

    UserResponseDto getUserDetailsById(long userId);

    UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto);

    void deleteUser(long userId);

    LoginResponseDto login(LoginRequestDto requestDto);

    User getUserById(long userId);

    void addExerciseToUser(long userId, Exercise exercise);

    void deleteUserExercise(long userId, Exercise exercise);

    void addWorkout(User user, Workout workout);

    void removeWorkout(User user, Workout workout);
}
