package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public void deleteAll() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        workoutRepository.deleteAll();
        exerciseRepository.deleteAll();
        bodyPartRepository.deleteAll();
        httpRefRepository.deleteAll();
        countryRepository.deleteAll();
    }

    public BodyPart createBodyPart(int seed) {
        return bodyPartRepository.save(BodyPart.builder().name("Name " + seed).build());
    }

    public HttpRef createHttpRef(int seed, boolean isCustom) {
        return httpRefRepository.save(HttpRef.builder()
                .name("Name " + seed)
                .ref("Ref " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .build());
    }

    public Exercise createExercise(
            int seed, boolean isCustom, boolean needsEquipment, Set<BodyPart> bodyParts, Set<HttpRef> httpRefs) {
        return exerciseRepository.save(Exercise.builder()
                .title("Title " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .needsEquipment(needsEquipment)
                .bodyParts(bodyParts)
                .httpRefs(httpRefs)
                .build());
    }

    public Exercise exerciseAddUsers(Exercise exercise, Set<User> users) {
        exercise.setUsers(users);
        return exerciseRepository.save(exercise);
    }

    public Role createRole(String name) {
        return roleRepository.save(new Role(name));
    }

    public User createUser(String seed, Role role, Country country, Set<Exercise> exercises) {
        return userRepository.save(new User.Builder()
                .username("username-" + seed)
                .fullName("Full Name " + seed)
                .email("username-" + seed + "@email.com")
                .password(passwordEncoder().encode("password-" + seed))
                .role(role)
                .exercises(exercises)
                .country(country)
                .build());
    }

    public Country createCountry(int seed) {
        return countryRepository.save(Country.builder().name("Country " + seed).build());
    }

    public Workout createWorkout(int seed, boolean isCustom, Set<Exercise> exercises) {
        Workout workout = Workout.builder()
                .title("Title " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .exercises(exercises)
                .build();
        return workoutRepository.save(workout);
    }
}
