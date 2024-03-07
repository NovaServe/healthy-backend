package healthy.lifestyle.backend.reminder.workout.repository;

import healthy.lifestyle.backend.reminder.workout.model.WorkoutCompletionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutCompletionRecordRepository extends JpaRepository<WorkoutCompletionRecord, Long> {}
