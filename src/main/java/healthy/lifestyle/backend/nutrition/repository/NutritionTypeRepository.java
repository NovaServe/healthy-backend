package healthy.lifestyle.backend.nutrition.repository;

import healthy.lifestyle.backend.nutrition.model.NutritionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionTypeRepository extends JpaRepository<NutritionType, Long> {}
