package healthy.lifestyle.backend.activity.workout.service;

import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.activity.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.activity.workout.repository.WorkoutRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemovalServiceImpl implements RemovalService {
    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    HttpRefRepository httpRefRepository;

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
