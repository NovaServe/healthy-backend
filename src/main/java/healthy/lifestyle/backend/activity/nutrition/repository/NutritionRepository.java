package healthy.lifestyle.backend.activity.nutrition.repository;

import healthy.lifestyle.backend.activity.nutrition.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionRepository extends JpaRepository<Nutrition, Long> {}
