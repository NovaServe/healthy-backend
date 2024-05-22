package healthy.lifestyle.backend.plan.workout.repository;

import healthy.lifestyle.backend.plan.workout.model.WorkoutPlanDayId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutDayIdRepository extends JpaRepository<WorkoutPlanDayId, Long> {}
