package healthy.lifestyle.backend.workout.repository;

import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.base.JpaBaseTest;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see ExerciseRepository
 */
class ExerciseRepositoryTest extends JpaBaseTest {
    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    DataHelper dataHelper;

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void findByTitleAndUserId_Positive() {
        // Data
        BodyPart bodyPart1 = dataHelper.createBodyPart("Arms");
        BodyPart bodyPart2 = dataHelper.createBodyPart("Triceps");

        HttpRef httpRef1 = dataHelper.createHttpRef("Media1", "https://ref1.com", "Description1");
        HttpRef httpRef2 = dataHelper.createHttpRef("Media2", "https://ref2.com", "Description2");
        HttpRef httpRef3 = dataHelper.createHttpRef("Media3", "https://ref3.com");

        Exercise exercise = dataHelper.createExercise(
                "Narrow push-ups",
                "Train triceps",
                true,
                Set.of(bodyPart1, bodyPart2),
                Set.of(httpRef1, httpRef2, httpRef3));

        bodyPart1.setExercises(Set.of(exercise));
        bodyPart2.setExercises(Set.of(exercise));

        Role role = dataHelper.createRole("ROLE_USER");
        User user =
                dataHelper.createUser("my-username", "My User", "user@email.com", "password", role, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        // Test
        Optional<Exercise> actualOpt = exerciseRepository.findByTitleAndUserId(exercise.getTitle(), user.getId());
        assertTrue(actualOpt.isPresent());
        Exercise actual = actualOpt.get();
        assertEquals(exercise.getTitle(), actual.getTitle());
        assertEquals(exercise.getDescription(), actual.getDescription());
        assertEquals(exercise.getIsCustom(), actual.getIsCustom());

        assertEquals(exercise.getBodyParts().size(), actual.getBodyParts().size());
        List<BodyPart> initialBodyParts = Arrays.asList(bodyPart1, bodyPart2);
        initialBodyParts.sort(Comparator.comparing(BodyPart::getName));

        List<BodyPart> actualBodyParts = new ArrayList<>(actual.getBodyParts());
        actualBodyParts.sort(Comparator.comparing(BodyPart::getName));

        for (int i = 0; i < initialBodyParts.size(); i++) {
            assertEquals(
                    initialBodyParts.get(i).getName(), actualBodyParts.get(i).getName());
        }

        assertEquals(exercise.getHttpRefs().size(), actual.getHttpRefs().size());
        List<HttpRef> initialHttpRefs = Arrays.asList(httpRef1, httpRef2, httpRef3);
        initialHttpRefs.sort(Comparator.comparing(HttpRef::getName));

        List<HttpRef> actualHttpRefs = new ArrayList<>(actual.getHttpRefs());
        actualHttpRefs.sort(Comparator.comparing(HttpRef::getName));

        for (int i = 0; i < initialHttpRefs.size(); i++) {
            assertEquals(initialHttpRefs.get(i).getName(), actualHttpRefs.get(i).getName());
            assertEquals(
                    initialHttpRefs.get(i).getDescription(),
                    actualHttpRefs.get(i).getDescription());
            assertEquals(initialHttpRefs.get(i).getRef(), actualHttpRefs.get(i).getRef());
        }

        assertEquals(exercise.getUsers().size(), actual.getUsers().size());
    }

    @Test
    void findByTitleAndUserId_Negative_WrongTitle() {
        // Test Data
        BodyPart bodyPart1 = dataHelper.createBodyPart("Arms");
        BodyPart bodyPart2 = dataHelper.createBodyPart("Triceps");

        HttpRef httpRef1 = dataHelper.createHttpRef("Media1", "https://ref1.com", "Description1");
        HttpRef httpRef2 = dataHelper.createHttpRef("Media2", "https://ref2.com", "Description2");
        HttpRef httpRef3 = dataHelper.createHttpRef("Media3", "https://ref3.com");

        Exercise exercise = dataHelper.createExercise(
                "Narrow push-ups",
                "Train triceps",
                true,
                Set.of(bodyPart1, bodyPart2),
                Set.of(httpRef1, httpRef2, httpRef3));

        Role role = dataHelper.createRole("ROLE_USER");
        User user =
                dataHelper.createUser("my-username", "My User", "user@email.com", "password", role, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        // Test
        Optional<Exercise> actualOpt = exerciseRepository.findByTitleAndUserId("Wrong title", user.getId());
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findByTitleAndUserId_Negative_WrongUserId() {
        // Test Data
        BodyPart bodyPart1 = dataHelper.createBodyPart("Arms");
        BodyPart bodyPart2 = dataHelper.createBodyPart("Triceps");

        HttpRef httpRef1 = dataHelper.createHttpRef("Media1", "https://ref1.com", "Description1");
        HttpRef httpRef2 = dataHelper.createHttpRef("Media2", "https://ref2.com", "Description2");
        HttpRef httpRef3 = dataHelper.createHttpRef("Media3", "https://ref3.com");

        Exercise exercise = dataHelper.createExercise(
                "Narrow push-ups",
                "Train triceps",
                true,
                Set.of(bodyPart1, bodyPart2),
                Set.of(httpRef1, httpRef2, httpRef3));

        Role role = dataHelper.createRole("ROLE_USER");
        User user =
                dataHelper.createUser("my-username", "My User", "user@email.com", "password", role, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        // Test
        Optional<Exercise> actualOpt = exerciseRepository.findByTitleAndUserId(exercise.getTitle(), 111L);
        assertTrue(actualOpt.isEmpty());
    }

    @Test
    void findByTitleAndUserId_Negative_NotCustom() {
        // Test Data
        BodyPart bodyPart1 = dataHelper.createBodyPart("Arms");
        BodyPart bodyPart2 = dataHelper.createBodyPart("Triceps");

        HttpRef httpRef1 = dataHelper.createHttpRef("Media1", "https://ref1.com", "Description1");
        HttpRef httpRef2 = dataHelper.createHttpRef("Media2", "https://ref2.com", "Description2");
        HttpRef httpRef3 = dataHelper.createHttpRef("Media3", "https://ref3.com");

        Exercise exercise = dataHelper.createExercise(
                "Narrow push-ups",
                "Train triceps",
                false,
                Set.of(bodyPart1, bodyPart2),
                Set.of(httpRef1, httpRef2, httpRef3));

        Role role = dataHelper.createRole("ROLE_USER");
        User user =
                dataHelper.createUser("my-username", "My User", "user@email.com", "password", role, Set.of(exercise));
        dataHelper.exerciseAddUsers(exercise, Set.of(user));

        // Test
        Optional<Exercise> actualOpt = exerciseRepository.findByTitleAndUserId(exercise.getTitle(), user.getId());
        assertTrue(actualOpt.isEmpty());
    }
}
