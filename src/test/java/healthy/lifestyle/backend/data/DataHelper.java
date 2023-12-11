package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

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

    //    @Autowired
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void deleteAll() {
        String deleteUsersExercises = "DELETE FROM users_exercises";
        entityManager.createNativeQuery(deleteUsersExercises).executeUpdate();

        String deleteWorkoutsExercises = "DELETE FROM workouts_exercises;";
        entityManager.createNativeQuery(deleteWorkoutsExercises).executeUpdate();

        String deleteUsersWorkouts = "DELETE FROM users_workouts;";
        entityManager.createNativeQuery(deleteUsersWorkouts).executeUpdate();

        String deleteWorkouts = "DELETE FROM workouts;";
        entityManager.createNativeQuery(deleteWorkouts).executeUpdate();

        String deleteExercisesBodyParts = "DELETE FROM exercises_body_parts;";
        entityManager.createNativeQuery(deleteExercisesBodyParts).executeUpdate();

        String deleteExercisesHttpRefs = "DELETE FROM exercises_http_refs;";
        entityManager.createNativeQuery(deleteExercisesHttpRefs).executeUpdate();

        String deleteExercises = "DELETE FROM exercises;";
        entityManager.createNativeQuery(deleteExercises).executeUpdate();

        String deleteBodyParts = "DELETE FROM body_parts;";
        entityManager.createNativeQuery(deleteBodyParts).executeUpdate();

        String deleteHttpRefs = "DELETE FROM http_refs;";
        entityManager.createNativeQuery(deleteHttpRefs).executeUpdate();

        String deleteUsers = "DELETE FROM users;";
        entityManager.createNativeQuery(deleteUsers).executeUpdate();

        String deleteRoles = "DELETE FROM roles;";
        entityManager.createNativeQuery(deleteRoles).executeUpdate();

        String deleteCountries = "DELETE FROM countries;";
        entityManager.createNativeQuery(deleteCountries).executeUpdate();
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

    public HttpRef httpRefAddUser(HttpRef httpRef, User user) {
        httpRef.setUser(user);
        return httpRefRepository.save(httpRef);
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

    public Exercise updateExercise(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    public Role createRole(String name) {
        return roleRepository.save(Role.builder().name(name).build());
    }

    public User createUser(String seed, Role role, Country country, Set<Exercise> exercises, Integer age) {
        return userRepository.save(User.builder()
                .username("username-" + seed)
                .fullName("Full Name " + seed)
                .email("username-" + seed + "@email.com")
                .password(passwordEncoder().encode("password-" + seed))
                .role(role)
                .exercises(exercises)
                .country(country)
                .age(age)
                .build());
    }

    public User createUser(String seed, Set<Exercise> exercises) {
        Role role = createRole("ROLE_USER");
        Country country = createCountry(1);
        return createUser(seed, role, country, exercises, 20);
    }

    public User getUserById(Long id) {
        return userRepository.getReferenceById(id);
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

    public void userAddWorkout(User user, Set<Workout> workouts) {
        user.setWorkouts(workouts);
        userRepository.save(user);
    }

    public Workout updateWorkout(Workout workout) {
        return workoutRepository.save(workout);
    }

    public void userAddExercises(User user, Set<Exercise> exercises) {
        user.setExercises(exercises);
        userRepository.save(user);
    }

    public UserUpdateRequestDto createUpdateUserRequestDto(String seed, Long countryId, Integer age) {
        return UserUpdateRequestDto.builder()
                .username("username-" + seed)
                .email("username-" + seed + "@email.com")
                .password("password-" + seed)
                .confirmPassword("password-" + seed)
                .fullName("Full Name " + seed)
                .countryId(countryId)
                .age(age)
                .build();
    }
}
