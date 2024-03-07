package healthy.lifestyle.backend.activity.workout.api;

import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;

public interface WorkoutApi {
    Workout getWorkoutById(long workoutId);

    Exercise getExerciseById(long exerciseId);
}
