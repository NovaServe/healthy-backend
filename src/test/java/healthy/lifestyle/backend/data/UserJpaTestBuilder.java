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
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestComponent
public class UserJpaTestBuilder {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private BodyPartRepository bodyPartRepository;

    @Autowired
    private HttpRefRepository httpRefRepository;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public UserWrapper getWrapper() {
        return new UserWrapper(
                new ExerciseJpaTestBuilder()
                        .getWrapper(this.exerciseRepository, this.bodyPartRepository, this.httpRefRepository),
                this.userRepository,
                this.roleRepository,
                this.countryRepository,
                this.passwordEncoder());
    }

    public static class UserWrapper {
        private final ExerciseJpaTestBuilder.ExerciseWrapper exerciseWrapper;

        private final UserRepository userRepository;

        private final RoleRepository roleRepository;

        private final CountryRepository countryRepository;

        private final PasswordEncoder passwordEncoder;

        private User user;

        private Integer userSeed;

        private String role;

        // Exercise related fields
        private Integer exerciseSeed;

        private Integer startSeedForExerciseNestedEntities;

        private Integer amountOfExerciseNestedEntities;

        private Integer amountOfExercises;

        private Integer startSeedForExercises;

        private Boolean exerciseCustom;

        private Boolean mediaCustom;

        private Boolean exerciseNeedsEquipment;

        public UserWrapper(
                ExerciseJpaTestBuilder.ExerciseWrapper exerciseWrapper,
                UserRepository userRepository,
                RoleRepository roleRepository,
                CountryRepository countryRepository,
                PasswordEncoder passwordEncoder) {
            this.exerciseWrapper = exerciseWrapper;
            this.userRepository = userRepository;
            this.roleRepository = roleRepository;
            this.countryRepository = countryRepository;
            this.passwordEncoder = passwordEncoder;
        }

        /**
         * User Related Functionality
         */
        public UserWrapper setRole(String role) {
            this.role = role;
            return this;
        }

        public UserWrapper setRoleUser() {
            this.role = "ROLE_USER";
            return this;
        }

        public UserWrapper setUserSeed(Integer userSeed) {
            if (userSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.userSeed = userSeed;
            return this;
        }

        public UserWrapper buildUser() {
            if (this.userSeed == null || this.role == null)
                throw new IllegalStateException("Not all required parameters are set");

            Role role = Role.builder().name(this.role).build();
            Role roleSaved = roleRepository.save(role);

            Country country = Country.builder()
                    .name("Country-" + ThreadLocalRandom.current().nextInt(1, 100))
                    .build();
            Country countrySaved = countryRepository.save(country);

            User user = User.builder()
                    .fullName("Full name " + this.userSeed)
                    .age(ThreadLocalRandom.current().nextInt(18, 100))
                    .username("Username-" + this.userSeed)
                    .email("email-" + this.userSeed + "@email.com")
                    .password(this.passwordEncoder.encode("password-" + this.userSeed))
                    .role(roleSaved)
                    .country(countrySaved)
                    .httpRefs(new HashSet<>())
                    .exercises(new HashSet<>())
                    .workouts(new HashSet<>())
                    .build();

            this.user = userRepository.save(user);
            return this;
        }

        public UserWrapper addCustomMedias(List<HttpRef> medias) {
            this.user.getHttpRefs().addAll(medias);
            this.user = userRepository.save(this.user);
            return this;
        }

        public User getUser() {
            return this.user;
        }

        public long getUserId() {
            if (this.user == null) throw new IllegalStateException("Not all required parameters are set");
            return this.user.getId();
        }

        /**
         * Common Exercise Functionality
         */
        public UserWrapper setExerciseNeedsEquipment(boolean exerciseNeedsEquipment) {
            this.exerciseNeedsEquipment = exerciseNeedsEquipment;
            return this;
        }

        public UserWrapper setExerciseCustom(Boolean exerciseCustom) {
            this.exerciseCustom = exerciseCustom;
            return this;
        }

        public UserWrapper setMediaCustom(Boolean mediaCustom) {
            this.mediaCustom = mediaCustom;
            return this;
        }

        public UserWrapper setAmountOfExerciseNestedEntities(Integer amountOfExerciseNestedEntities) {
            if (amountOfExerciseNestedEntities < 1) throw new IllegalArgumentException("Must be >= 1");
            this.amountOfExerciseNestedEntities = amountOfExerciseNestedEntities;
            return this;
        }

        public UserWrapper setStartSeedForExerciseNestedEntities(Integer startIdForExerciseNestedEntities) {
            if (startIdForExerciseNestedEntities < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startSeedForExerciseNestedEntities = startIdForExerciseNestedEntities;
            return this;
        }

        /**
         * Single Exercise Functionality
         */
        public UserWrapper setExerciseSeed(Integer exerciseSeed) {
            if (exerciseSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.exerciseSeed = exerciseSeed;
            return this;
        }

        public UserWrapper buildUserAndAddSingleExercise() {
            if (this.exerciseCustom == null
                    || !this.exerciseCustom
                    || this.exerciseNeedsEquipment == null
                    || this.mediaCustom == null
                    || this.amountOfExerciseNestedEntities == null
                    || this.startSeedForExerciseNestedEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            this.buildUser();
            exerciseWrapper
                    .setSeed(this.exerciseSeed)
                    .setExerciseCustom(this.exerciseCustom)
                    .setNeedsEquipment(this.exerciseNeedsEquipment)
                    .setMediaCustom(this.mediaCustom)
                    .setAmountOfNestedEntities(this.amountOfExerciseNestedEntities)
                    .setStartSeedForNestedEntities(this.startSeedForExerciseNestedEntities)
                    .buildSingle();
            if (this.mediaCustom) {
                this.user.getHttpRefs().addAll(exerciseWrapper.getSortedMedias());
                this.user = userRepository.save(this.user);
            }
            this.user.getExercises().addAll(exerciseWrapper.getExerciseCollection());
            this.user = userRepository.save(this.user);
            return this;
        }

        public Exercise getSingleExercise() {
            Optional<Exercise> exerciseOptional =
                    this.user.getExercises().stream().findFirst();
            return exerciseOptional.orElse(null);
        }

        public Long getSingleExerciseId() {
            Optional<Exercise> exerciseOptional =
                    this.user.getExercises().stream().findFirst();
            return exerciseOptional.map(Exercise::getId).orElse(null);
        }

        public BodyPart getBodyPartFromSingleExercise(int bodyPartIndex) {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        public HttpRef getMediaFromSingleExercise(int mediaIndex) {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex);
        }

        public long getBodyPartIdFromSingleExercise(int bodyPartIndex) {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex)
                    .getId();
        }

        public long getMediaIdFromSingleExercise(int mediaIndex) {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex)
                    .getId();
        }

        public List<BodyPart> getBodyPartsSortedFromSingleExercise() {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        public List<HttpRef> getMediasSortedFromSingleExercise() {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        /**
         * Exercises Functionality
         */
        public UserWrapper setAmountOfExercises(Integer amountOfExercises) {
            if (amountOfExercises < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfExercises = amountOfExercises;
            return this;
        }

        public UserWrapper setStartSeedForExercises(Integer startIdForExercises) {
            if (startIdForExercises < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startSeedForExercises = startIdForExercises;
            return this;
        }

        public UserWrapper buildUserAndAddExercises() {
            if (this.exerciseCustom == null
                    || !this.exerciseCustom
                    || this.exerciseNeedsEquipment == null
                    || this.mediaCustom == null
                    || this.amountOfExerciseNestedEntities == null
                    || this.startSeedForExerciseNestedEntities == null
                    || this.amountOfExercises == null
                    || this.startSeedForExercises == null)
                throw new IllegalStateException("Not all required parameters are set");

            this.buildUser();
            exerciseWrapper
                    .setAmountOfExercises(this.amountOfExercises)
                    .setStartSeedForExercises(this.startSeedForExercises)
                    .setExerciseCustom(this.exerciseCustom)
                    .setNeedsEquipment(this.exerciseNeedsEquipment)
                    .setMediaCustom(this.mediaCustom)
                    .setAmountOfNestedEntities(this.amountOfExerciseNestedEntities)
                    .setStartSeedForNestedEntities(this.startSeedForExerciseNestedEntities)
                    .buildSingle();
            if (this.mediaCustom) {
                this.user.getHttpRefs().addAll(exerciseWrapper.getSortedMedias());
                this.user = userRepository.save(this.user);
            }
            this.user.getExercises().addAll(exerciseWrapper.getExerciseCollection());
            this.user = userRepository.save(this.user);
            return this;
        }

        public UserWrapper addCustomExercises(List<Exercise> exercises) {
            if (exercises == null || exercises.size() == 0)
                throw new IllegalArgumentException("Must be >= 1 exercises");
            if (this.user == null) throw new IllegalStateException("User must not be null");
            this.user.getExercises().addAll(exercises);
            this.user = userRepository.save(this.user);
            return this;
        }

        public List<Exercise> getExercisesSorted() {
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList();
        }

        public Exercise getExerciseFromSortedList(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
        }

        public long getExerciseIdFromSortedList(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex)
                    .getId();
        }

        public Set<BodyPart> getDistinctBodyParts() {
            return exerciseWrapper.getDistinctBodyParts();
        }

        public List<BodyPart> getDistinctBodyPartsSorted() {
            return exerciseWrapper.getDistinctBodyPartsSorted();
        }

        public Set<HttpRef> getDistinctMedias() {
            return exerciseWrapper.getDistinctMedias();
        }

        public List<HttpRef> getDistinctMediasSorted() {
            return exerciseWrapper.getDistinctMediasSorted();
        }

        public BodyPart getBodyPartFromExerciseList(int exerciseIndex, int bodyPartIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        public HttpRef getMediaFromExerciseList(int exerciseIndex, int mediaIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex);
        }
    }
}
