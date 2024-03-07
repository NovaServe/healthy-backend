package healthy.lifestyle.backend.reminder.workout.service;

import java.time.LocalDate;

public interface WorkoutCompletionService {
    void setSingleWorkoutCompleted(long workoutId, LocalDate completedAt);

    void setExerciseCompleted(long workoutId, long exerciseId, LocalDate completedAt);
}
