package healthy.lifestyle.backend.activity.workout.api;

import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.activity.workout.repository.WorkoutRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class WorkoutApiImpl implements WorkoutApi {
    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Override
    public Workout getWorkoutById(long workoutId) {
        return workoutRepository
                .findById(workoutId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, workoutId, HttpStatus.NOT_FOUND));
    }

    @Override
    public Exercise getExerciseById(long exerciseId) {
        return exerciseRepository
                .findById(exerciseId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, exerciseId, HttpStatus.NOT_FOUND));
    }
}
