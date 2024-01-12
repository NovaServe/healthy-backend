package healthy.lifestyle.backend.nutrition.repository;

import healthy.lifestyle.backend.nutrition.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionRepository extends JpaRepository<Nutrition, Long> {}
