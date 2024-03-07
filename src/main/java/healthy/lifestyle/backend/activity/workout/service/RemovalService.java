package healthy.lifestyle.backend.activity.workout.service;

import java.util.List;

public interface RemovalService {
    void deleteCustomHttpRefs(List<Long> ids);

    void deleteCustomExercises(List<Long> ids);

    void deleteCustomWorkouts(List<Long> workoutIds);
}
