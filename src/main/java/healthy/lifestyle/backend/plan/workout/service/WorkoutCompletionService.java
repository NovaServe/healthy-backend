package healthy.lifestyle.backend.plan.workout.service;

import java.time.LocalDate;

public interface WorkoutCompletionService {
    void setSingleWorkoutCompleted(long workoutId, LocalDate completedAt);

    void setExerciseCompleted(long workoutId, long exerciseId, LocalDate completedAt);
}
