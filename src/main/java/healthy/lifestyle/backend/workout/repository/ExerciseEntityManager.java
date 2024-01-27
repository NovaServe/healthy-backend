package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.workout.model.Exercise;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExerciseEntityManager {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    SessionFactory sessionFactory;

    List<Exercise> findDefaultOrCustomWithFilter(
            boolean isCustom,
            Long userId,
            String title,
            String description,
            Boolean needsEquipment,
            List<Long> bodyPartsIds) {
        //        String query = "SELECT e FROM Exercise e JOIN e.bodyParts bp "
        //                + "WHERE bp.id >= 0 AND (:bodyPartsIds IS NULL OR bp.id IN :bodyPartsIds) "
        //                + "AND (:userId IS NULL OR e.user.id = :userId) AND e.isCustom = :isCustom "
        //                + "AND (:title IS NULL OR e.title ILIKE CONCAT('%', :title, '%')) "
        //                + "AND (:description IS NULL OR e.description ILIKE CONCAT('%', :description, '%')) "
        //                + "AND (:needsEquipment IS NULL OR e.needsEquipment = :needsEquipment)"
        //                ;

        //        Query query1 = entityManager.createQuery(query);
        //         query1
        //                .setParameter("bodyPartsIds", bodyPartsIds)
        //                .setParameter("isCustom", isCustom)
        //                .setParameter("userId", userId)
        //                .setParameter("title", title)
        //                .setParameter("description", description)
        //                .setParameter("needsEquipment", needsEquipment)
        //         ;

        String query = "SELECT e FROM Exercise e JOIN e.bodyParts bp WHERE bp.id >= 0 AND (bp.id IN :bodyPartsIds)";
        AtomicReference<List<Exercise>> exercises = new AtomicReference<>();
        sessionFactory.inSession(session -> exercises.set(session.createQuery(query, Exercise.class)
                .setParameter("bodyPartsIds", null)
                .getResultList()));

        return null;
    }
}
