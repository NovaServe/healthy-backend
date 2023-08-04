package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class DataHelper {
    @Autowired
    BodyPartRepository bodyPartRepository;

    @Autowired
    HttpRefRepository httpRefRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    public void deleteAll() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        exerciseRepository.deleteAll();
        bodyPartRepository.deleteAll();
        httpRefRepository.deleteAll();
    }

    public BodyPart createBodyPart(String name) {
        BodyPart bodyPart = new BodyPart.Builder().name(name).build();
        return bodyPartRepository.save(bodyPart);
    }

    public HttpRef createHttpRef(String name, String ref, String description) {
        HttpRef httpRef1 = new HttpRef.Builder()
                .name(name)
                .ref(ref)
                .description(description)
                .build();
        return httpRefRepository.save(httpRef1);
    }

    public HttpRef createHttpRef(String name, String ref) {
        return this.createHttpRef(name, ref, null);
    }

    public Exercise createExercise(
            String title, String description, boolean isCustom, Set<BodyPart> bodyParts, Set<HttpRef> httpRefs) {

        Exercise exercise = new Exercise.Builder()
                .title(title)
                .description(description)
                .isCustom(isCustom)
                .bodyParts(bodyParts)
                .httpRefs(httpRefs)
                .build();
        return exerciseRepository.save(exercise);
    }

    public Exercise exerciseAddUsers(Exercise exercise, Set<User> users) {
        exercise.setUsers(users);
        return exerciseRepository.save(exercise);
    }

    public Role createRole(String name) {
        Role role = new Role(name);
        return roleRepository.save(role);
    }

    public User createUser(
            String fullName, String username, String email, String password, Role role, Set<Exercise> exercises) {
        User user = new User.Builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .password(password)
                .role(role)
                .exercises(exercises)
                .build();
        return userRepository.save(user);
    }
}
