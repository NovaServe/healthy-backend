package healthy.lifestyle.backend.activity.mental.repository;

import healthy.lifestyle.backend.activity.mental.model.MentalWorkout;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentalWorkoutRepository extends JpaRepository<MentalWorkout, Long> {

    @Query("SELECT w FROM MentalWorkout w WHERE (w.title = :title AND w.isCustom = true AND w.user.id = :userId) "
            + "OR (w.title = :title AND w.isCustom = false)")
    List<MentalWorkout> findDefaultAndCustomByTitleAndUserId(String title, Long userId);

    @Query(
            "SELECT DISTINCT w FROM MentalWorkout w WHERE w.isCustom = false OR (w.isCustom = true AND w.user.id = :userId) ")
    Page<MentalWorkout> findDefaultAndCustomMentalWorkouts(@Param("userId") Long userId, Pageable pageable);
}
