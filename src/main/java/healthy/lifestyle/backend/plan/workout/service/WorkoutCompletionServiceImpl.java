package healthy.lifestyle.backend.plan.workout.service;

import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutCompletionRecordRepository;
import healthy.lifestyle.backend.plan.workout.model.WorkoutCompletionRecord;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkoutCompletionServiceImpl implements WorkoutCompletionService {
    @Autowired
    WorkoutCompletionRecordRepository workoutCompletionRecordRepository;

    @Autowired
    WorkoutApi workoutApi;

    @Override
    public void setSingleWorkoutCompleted(long workoutId, LocalDate completedAt) {
        Workout workout = workoutApi.getWorkoutById(workoutId);
        WorkoutCompletionRecord workoutCompletionRecord = WorkoutCompletionRecord.builder()
                .workout(workout)
                .completedAt(completedAt)
                .build();
        workoutCompletionRecordRepository.save(workoutCompletionRecord);
    }

    @Override
    public void setExerciseCompleted(long workoutId, long exerciseId, LocalDate completedAt) {
        Workout workout = workoutApi.getWorkoutById(workoutId);
        Exercise exercise = workoutApi.getExerciseById(exerciseId);
        WorkoutCompletionRecord workoutCompletionRecord = WorkoutCompletionRecord.builder()
                .workout(workout)
                .exercise(exercise)
                .completedAt(completedAt)
                .build();
        workoutCompletionRecordRepository.save(workoutCompletionRecord);
    }
}
