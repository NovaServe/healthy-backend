package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemovalServiceImpl implements RemovalService {
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final HttpRefRepository httpRefRepository;

    public RemovalServiceImpl(
            WorkoutRepository workoutRepository,
            ExerciseRepository exerciseRepository,
            HttpRefRepository httpRefRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.httpRefRepository = httpRefRepository;
    }

    @Override
    @Transactional
    public void deleteCustomHttpRefs(List<Long> ids) {
        httpRefRepository.deleteAllById(ids);
    }

    @Override
    @Transactional
    public void deleteCustomExercises(List<Long> ids) {
        exerciseRepository.deleteAllById(ids);
    }

    @Override
    @Transactional
    public void deleteCustomWorkouts(List<Long> workoutIds) {
        workoutRepository.deleteAllById(workoutIds);
    }
}
