package healthy.lifestyle.backend.plan.workout.repository;

import healthy.lifestyle.backend.plan.workout.model.WorkoutCompletionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutCompletionRecordRepository extends JpaRepository<WorkoutCompletionRecord, Long> {}
