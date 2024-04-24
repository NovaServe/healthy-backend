package healthy.lifestyle.backend.activity.nutrition.repository;

import healthy.lifestyle.backend.activity.nutrition.model.NutritionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionTypeRepository extends JpaRepository<NutritionType, Long> {}
