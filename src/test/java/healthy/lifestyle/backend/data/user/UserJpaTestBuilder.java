package healthy.lifestyle.backend.data.user;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.data.exercise.ExerciseJpaTestBuilder;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
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
import org.springframework.http.HttpStatus;
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

    public static class UserWrapper implements UserTestWrapperBase {
        private final ExerciseJpaTestBuilder.ExerciseWrapper exerciseWrapper;

        private Integer userIdOrSeed;

        private String role;

        private Integer roleId;

        private User user;

        private boolean isRoleAlreadyCreated;

        private Integer exerciseIdOrSeed;

        private Boolean isExerciseCustom;

        private Boolean isExerciseNeedsEquipment;

        private Boolean isExerciseHttpRefsCustom;

        private Integer amountOfExercises;

        private Integer amountOfExerciseNestedEntities;

        private Integer startIdOrSeedForExerciseNestedEntities;

        private final UserRepository userRepository;

        private final RoleRepository roleRepository;

        private final CountryRepository countryRepository;

        private final PasswordEncoder passwordEncoder;

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

        @Override
        public UserWrapper setUserIdOrSeed(int userIdOrSeed) {
            if (userIdOrSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.userIdOrSeed = userIdOrSeed;
            return this;
        }

        @Override
        public UserWrapper setUserRole() {
            this.role = "ROLE_USER";
            return this;
        }

        @Override
        public UserWrapper setAdminRole() {
            this.role = "ROLE_ADMIN";
            return this;
        }

        @Override
        public UserWrapper setRoleId(int roleId) {
            if (roleId < 0) throw new IllegalArgumentException("Must be >= 0");
            this.roleId = roleId;
            return this;
        }

        @Override
        public UserWrapper setIsRoleAlreadyCreated(boolean isRoleAlreadyCreated) {
            this.isRoleAlreadyCreated = isRoleAlreadyCreated;
            return this;
        }

        @Override
        public UserWrapper setExerciseIdOrSeed(int exerciseIdOrSeed) {
            if (exerciseIdOrSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.exerciseIdOrSeed = exerciseIdOrSeed;
            return this;
        }

        @Override
        public UserWrapper setIsExerciseCustom(boolean isExerciseCustom) {
            this.isExerciseCustom = isExerciseCustom;
            return this;
        }

        @Override
        public UserWrapper setIsExerciseNeedsEquipment(boolean isExerciseNeedsEquipment) {
            this.isExerciseNeedsEquipment = isExerciseNeedsEquipment;
            return this;
        }

        @Override
        public UserWrapper setIsExerciseHttpRefsCustom(boolean setIsExerciseHttpRefsCustom) {
            this.isExerciseHttpRefsCustom = setIsExerciseHttpRefsCustom;
            return this;
        }

        @Override
        public UserWrapper setAmountOfExercises(int amountOfExercises) {
            if (amountOfExercises < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfExercises = amountOfExercises;
            return this;
        }

        @Override
        public UserWrapper setAmountOfExerciseNestedEntities(int amountOfExerciseNestedEntities) {
            if (amountOfExerciseNestedEntities < 1) throw new IllegalArgumentException("Must be >= 1");
            this.amountOfExerciseNestedEntities = amountOfExerciseNestedEntities;
            return this;
        }

        @Override
        public UserWrapper setStartIdOrSeedForExerciseNestedEntities(int startIdOrSeedForExerciseNestedEntities) {
            if (startIdOrSeedForExerciseNestedEntities < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startIdOrSeedForExerciseNestedEntities = startIdOrSeedForExerciseNestedEntities;
            return this;
        }

        @Override
        public User buildUser() {
            if (this.userIdOrSeed == null || this.role == null)
                throw new IllegalStateException("Not all required parameters are set");

            Role role = null;
            if (this.isRoleAlreadyCreated)
                role = roleRepository
                        .findByName(this.role)
                        .orElseThrow(() -> new IllegalStateException("User role not found"));
            else {
                Role roleBuilt = Role.builder().name(this.role).build();
                role = roleRepository.save(roleBuilt);
            }

            Country country =
                    Country.builder().name("Country-" + this.userIdOrSeed).build();
            Country countrySaved = countryRepository.save(country);

            User user = User.builder()
                    .fullName("Full name " + this.userIdOrSeed)
                    .age(ThreadLocalRandom.current().nextInt(18, 100))
                    .username("Username-" + this.userIdOrSeed)
                    .email("email-" + this.userIdOrSeed + "@email.com")
                    .password(this.passwordEncoder.encode("password-" + this.userIdOrSeed))
                    .role(role)
                    .country(countrySaved)
                    .httpRefs(new HashSet<>())
                    .exercises(new HashSet<>())
                    .workouts(new HashSet<>())
                    .build();

            this.user = userRepository.save(user);
            return this.user;
        }

        @Override
        public User buildUserAndAddSingleExercise() {
            if (this.isExerciseCustom == null
                    || !this.isExerciseCustom
                    || this.isExerciseNeedsEquipment == null
                    || this.isExerciseHttpRefsCustom == null
                    || this.amountOfExerciseNestedEntities == null
                    || this.startIdOrSeedForExerciseNestedEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            this.buildUser();
            exerciseWrapper
                    .setIdOrSeed(this.exerciseIdOrSeed)
                    .setIsExerciseCustom(this.isExerciseCustom)
                    .setNeedsEquipment(this.isExerciseNeedsEquipment)
                    .setIsHttpRefCustom(this.isExerciseHttpRefsCustom)
                    .setAmountOfNestedEntities(this.amountOfExerciseNestedEntities)
                    .setStartIdOrSeedForNestedEntities(this.startIdOrSeedForExerciseNestedEntities)
                    .buildSingle();
            if (this.isExerciseHttpRefsCustom) {
                this.user.getHttpRefs().addAll(exerciseWrapper.getHttpRefsSortedFromSingle());
                this.user = userRepository.save(this.user);
            }
            this.user.getExercises().addAll(exerciseWrapper.getExerciseSingleAsList());
            this.user = userRepository.save(this.user);
            return this.user;
        }

        @Override
        public User buildUserAndAddMultipleExercises() {
            if (this.isExerciseCustom == null
                    || !this.isExerciseCustom
                    || this.isExerciseNeedsEquipment == null
                    || this.isExerciseHttpRefsCustom == null
                    || this.amountOfExerciseNestedEntities == null
                    || this.startIdOrSeedForExerciseNestedEntities == null
                    || this.amountOfExercises == null
                    || this.exerciseIdOrSeed == null)
                throw new IllegalStateException("Not all required parameters are set");

            this.buildUser();
            exerciseWrapper
                    .setAmountOfExercises(this.amountOfExercises)
                    .setIdOrSeed(this.exerciseIdOrSeed)
                    .setIsExerciseCustom(this.isExerciseCustom)
                    .setNeedsEquipment(this.isExerciseNeedsEquipment)
                    .setIsHttpRefCustom(this.isExerciseHttpRefsCustom)
                    .setAmountOfNestedEntities(this.amountOfExerciseNestedEntities)
                    .setStartIdOrSeedForNestedEntities(this.startIdOrSeedForExerciseNestedEntities)
                    .buildList();
            if (this.isExerciseCustom && this.isExerciseHttpRefsCustom) {
                this.user.getHttpRefs().addAll(exerciseWrapper.getDistinctHttpRefsFromList());
                this.user = userRepository.save(this.user);
            }
            this.user.getExercises().addAll(exerciseWrapper.getExercisesAll());
            this.user = userRepository.save(this.user);
            return this.user;
        }

        @Override
        public User getUser() {
            return this.user;
        }

        @Override
        public long getUserId() {
            if (isNull(this.user)) throw new IllegalStateException("Not all required parameters are set");
            return this.user.getId();
        }

        @Override
        public UserWrapper addCustomExercises(List<Exercise> exercises) {
            if (exercises == null || exercises.size() == 0)
                throw new IllegalArgumentException("Must be >= 1 exercises");
            if (this.user == null) throw new IllegalStateException("User must not be null");
            this.user.getExercises().addAll(exercises);
            this.user = userRepository.save(this.user);
            return this;
        }

        @Override
        public UserWrapper addCustomHttpRefs(List<HttpRef> medias) {
            this.user.getHttpRefs().addAll(medias);
            this.user = userRepository.save(this.user);
            return this;
        }

        @Override
        public Exercise getExerciseSingle() {
            return this.user.getExercises().stream().findFirst().orElse(null);
        }

        @Override
        public Long getExerciseIdSingle() {
            return this.user.getExercises().stream()
                    .findFirst()
                    .map(Exercise::getId)
                    .orElse(null);
        }

        @Override
        public Exercise getExerciseFromSortedList(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
        }

        @Override
        public Long getExerciseIdFromSortedList(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex)
                    .getId();
        }

        @Override
        public List<Exercise> getAllExercisesSorted() {
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList();
        }

        @Override
        public BodyPart getBodyPartByIndexFromSingleExercise(int bodyPartIndex) {
            return this.getExerciseSingle().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        @Override
        public long getBodyPartIdByIndexFromSingleExercise(int bodyPartIndex) {
            return this.getExerciseSingle().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex)
                    .getId();
        }

        @Override
        public List<BodyPart> getBodyPartsSortedFromSingleExercise() {
            return this.getExerciseSingle().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        @Override
        public List<Long> getBodyPartsIdsSortedFromSingleExercise() {
            return this.getExerciseSingle().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .map(BodyPart::getId)
                    .toList();
        }

        @Override
        public List<BodyPart> getBodyPartsSortedFromExerciseListByIndex(int exerciseIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        @Override
        public List<Long> getBodyPartsIdsSortedFromExerciseListByIndex(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");

            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .map(BodyPart::getId)
                    .toList();
        }

        @Override
        public BodyPart getBodyPartByIndexFromExerciseList(int exerciseIndex, int bodyPartIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        @Override
        public Set<BodyPart> getDistinctBodyPartsFromExerciseList() {
            return exerciseWrapper.getDistinctBodyPartsFromList();
        }

        @Override
        public List<BodyPart> getDistinctSortedBodyPartsFromExerciseList() {
            return exerciseWrapper.getDistinctBodyPartsSortedFromList();
        }

        @Override
        public HttpRef getHttpRefByIndexFromSingleExercise(int httpRefIndex) {
            return this.getExerciseSingle().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(httpRefIndex);
        }

        @Override
        public long getHttpRefIdByIndexFromSingleExercise(int httpRefIndex) {
            return this.getExerciseSingle().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(httpRefIndex)
                    .getId();
        }

        @Override
        public List<HttpRef> getHttpRefsSortedFromSingleExercise() {
            return this.getExerciseSingle().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        @Override
        public List<Long> getHttpRefsIdsSortedFromSingleExercise() {
            return this.getExerciseSingle().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .map(HttpRef::getId)
                    .toList();
        }

        @Override
        public List<HttpRef> getHttpRefsSortedFromExerciseListByIndex(int exerciseIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        @Override
        public List<Long> getHttpRefsIdsSortedFromExerciseListByIndex(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");

            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .map(HttpRef::getId)
                    .toList();
        }

        @Override
        public HttpRef getHttpRefByIndexFromExercisesList(int exerciseIndex, int httpRefIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);

            return exercise.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(httpRefIndex);
        }

        @Override
        public Set<HttpRef> getDistinctHttpRefsFromExerciseList() {
            return exerciseWrapper.getDistinctHttpRefsFromList();
        }

        @Override
        public List<HttpRef> getDistinctSortedHttpRefsFromExerciseList() {
            return exerciseWrapper.getDistinctHttpRefsSortedFromList();
        }

        public User getUserById(long userId) {
            return userRepository
                    .findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND));
        }
    }
}
