package healthy.lifestyle.backend.testutil;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.mental.model.MentalWorkout;
import healthy.lifestyle.backend.activity.mental.repository.MentalActivityRepository;
import healthy.lifestyle.backend.activity.mental.repository.MentalTypeRepository;
import healthy.lifestyle.backend.activity.mental.repository.MentalWorkoutRepository;
import healthy.lifestyle.backend.activity.nutrition.model.Nutrition;
import healthy.lifestyle.backend.activity.nutrition.model.NutritionType;
import healthy.lifestyle.backend.activity.nutrition.repository.NutritionRepository;
import healthy.lifestyle.backend.activity.nutrition.repository.NutritionTypeRepository;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefTypeEnum;
import healthy.lifestyle.backend.activity.workout.model.*;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.activity.workout.repository.*;
import healthy.lifestyle.backend.activity.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.activity.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.activity.workout.repository.WorkoutRepository;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutPlanRepository;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.CountryRepository;
import healthy.lifestyle.backend.user.repository.RoleRepository;
import healthy.lifestyle.backend.user.repository.TimezoneRepository;
import healthy.lifestyle.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@TestComponent
public class DbUtil implements Util {
    @Autowired
    BodyPartRepository bodyPartRepository;

    @Autowired
    HttpRefRepository httpRefRepository;

    @Autowired
    HttpRefTypeRepository httpRefTypeRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    TimezoneRepository timezoneRepository;

    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    MentalActivityRepository mentalRepository;

    @Autowired
    MentalTypeRepository mentalTypeRepository;

    @Autowired
    NutritionRepository nutritionRepository;

    @Autowired
    NutritionTypeRepository nutritionTypeRepository;

    @Autowired
    MentalWorkoutRepository mentalWorkoutRepository;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    SharedUtil sharedUtil = new SharedUtil();

    @Transactional
    public void deleteAll() {
        workoutPlanRepository.deleteAll();
        workoutRepository.deleteAll();
        exerciseRepository.deleteAll();
        bodyPartRepository.deleteAll();
        mentalRepository.deleteAll();
        mentalTypeRepository.deleteAll();
        mentalWorkoutRepository.deleteAll();
        nutritionRepository.deleteAll();
        nutritionTypeRepository.deleteAll();
        httpRefRepository.deleteAll();
        httpRefTypeRepository.deleteAll();
        userRepository.deleteAll();
        countryRepository.deleteAll();
        timezoneRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Override
    public BodyPart createBodyPart(int seed) {
        return bodyPartRepository.save(BodyPart.builder().name("Name " + seed).build());
    }

    @Override
    public HttpRef createDefaultHttpRef(int seed) {
        return this.createHttpRefBase(seed, false, HttpRefTypeEnum.YOUTUBE, null);
    }

    @Override
    public HttpRef createCustomHttpRef(int seed, User user) {
        HttpRef httpRef = this.createHttpRefBase(seed, true, HttpRefTypeEnum.YOUTUBE, user);
        if (user.getHttpRefs() == null) user.setHttpRefs(new HashSet<>());
        user.getHttpRefs().add(httpRef);
        userRepository.save(user);
        return httpRef;
    }

    private HttpRef createHttpRefBase(int seed, boolean isCustom, HttpRefTypeEnum httpRefTypeEnum, User user) {
        Optional<HttpRefType> httpRefTypeOptional = httpRefTypeRepository.findByName(httpRefTypeEnum.name());
        HttpRefType httpRefType;
        if (httpRefTypeOptional.isPresent()) {
            httpRefType = httpRefTypeOptional.get();
        } else {
            HttpRefType httpRefTypeNew =
                    HttpRefType.builder().name(httpRefTypeEnum.name()).build();
            httpRefType = httpRefTypeRepository.save(httpRefTypeNew);
        }
        return httpRefRepository.save(HttpRef.builder()
                .name("Media Name " + seed)
                .ref("https://ref " + seed + ".com")
                .httpRefType(httpRefType)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .build());
    }

    @Override
    public Exercise createDefaultExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs) {
        return this.createExerciseBase(seed, false, needsEquipment, bodyParts, httpRefs, null);
    }

    @Override
    public Exercise createCustomExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs, User user) {
        Exercise exercise = this.createExerciseBase(seed, true, needsEquipment, bodyParts, httpRefs, user);
        if (user.getExercises() == null) user.setExercises(new HashSet<>());
        user.getExercises().add(exercise);
        userRepository.save(user);
        return exercise;
    }

    private Exercise createExerciseBase(
            int seed,
            boolean isCustom,
            boolean needsEquipment,
            List<BodyPart> bodyParts,
            List<HttpRef> httpRefs,
            User user) {
        Exercise exercise = Exercise.builder()
                .title("Exercise " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .needsEquipment(needsEquipment)
                .user(user)
                .bodyParts(new HashSet<>(bodyParts))
                .httpRefs(new HashSet<>(httpRefs))
                .build();
        return exerciseRepository.save(exercise);
    }

    @Override
    public Workout createDefaultWorkout(int seed, List<Exercise> exercises) {
        return this.createWorkoutBase(seed, false, exercises, null);
    }

    @Override
    public Workout createCustomWorkout(int seed, List<Exercise> exercises, User user) {
        Workout workout = this.createWorkoutBase(seed, true, exercises, user);
        if (user.getWorkouts() == null) user.setWorkouts(new HashSet<>());
        user.getWorkouts().add(workout);
        userRepository.save(user);
        return workout;
    }

    private Workout createWorkoutBase(int seed, boolean isCustom, List<Exercise> exercises, User user) {
        Workout workout = Workout.builder()
                .title("Workout " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .exercises(new HashSet<>(exercises))
                .build();
        return workoutRepository.save(workout);
    }

    @Override
    public WorkoutPlan createWorkoutPlan(Long seed, User user, Workout workout) {
        LocalDate startDate = LocalDate.of(
                LocalDate.now().getYear(),
                LocalDate.now().getMonth(),
                LocalDate.now().getDayOfMonth());
        LocalDate endDate = LocalDate.of(
                LocalDate.now().getYear(),
                LocalDate.now().getMonth(),
                LocalDate.now().getDayOfMonth());

        JsonDescription jsonDescription = SharedUtil.createJsonDescription(seed.intValue());

        WorkoutPlan workoutPlan = WorkoutPlan.builder()
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .workout(workout)
                .jsonDescription(List.of(jsonDescription))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .deactivatedAt(null)
                .build();

        return workoutPlanRepository.save(workoutPlan);
    }

    @Override
    public User createUser(int seed) {
        Role role = roleRepository.save(Role.builder().name("ROLE_USER").build());
        Country country =
                countryRepository.save(Country.builder().name("Country-" + seed).build());
        Timezone timezone = createTimezone(seed);
        return this.createUserBase(seed, role, country, null, timezone, null, null, null);
    }

    @Override
    public User createUser(int seed, int age) {
        Role role = roleRepository.save(Role.builder().name("ROLE_USER").build());
        Country country =
                countryRepository.save(Country.builder().name("Country-" + seed).build());
        Timezone timezone = createTimezone(seed);
        return this.createUserBase(seed, role, country, age, timezone, null, null, null);
    }

    @Override
    public User createAdminUser(int seed) {
        Role role = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Country country =
                countryRepository.save(Country.builder().name("Country-" + seed).build());
        Timezone timezone = createTimezone(seed);
        return this.createUserBase(seed, role, country, null, timezone, null, null, null);
    }

    @Override
    public User createUser(int seed, Role role, Country country, Timezone timezone) {
        return this.createUserBase(seed, role, country, null, timezone, null, null, null);
    }

    @Override
    public User createUser(int seed, Role role, Country country, int age, Timezone timezone) {
        return this.createUserBase(seed, role, country, age, timezone, null, null, null);
    }

    private User createUserBase(
            int seed,
            Role role,
            Country country,
            Integer age,
            Timezone timezone,
            List<HttpRef> httpRefs,
            List<Exercise> exercises,
            List<Workout> workouts) {
        int AGE_CONST = 20;
        User user = User.builder()
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .fullName("Full Name " + SharedUtil.numberToText(seed))
                .role(role)
                .country(country)
                .age(isNull(age) ? AGE_CONST + seed : age)
                .timezone(timezone)
                .password(passwordEncoder().encode("Password-" + seed))
                .httpRefs(isNull(httpRefs) ? null : new HashSet<>(httpRefs))
                .exercises(isNull(exercises) ? null : new HashSet<>(exercises))
                .workouts(isNull(workouts) ? null : new HashSet<>(workouts))
                .build();
        return userRepository.save(user);
    }

    @Override
    public Role createUserRole() {
        return this.createRoleBase("USER");
    }

    @Override
    public Role createAdminRole() {
        return this.createRoleBase("ADMIN");
    }

    private Role createRoleBase(String role) {
        return roleRepository.save(Role.builder().name("ROLE_" + role).build());
    }

    @Override
    public Country createCountry(int seed) {
        return countryRepository.save(Country.builder().name("Country " + seed).build());
    }

    @Override
    public Timezone createTimezone() {
        return createTimezone(1);
    }

    @Override
    public Timezone createTimezone(int seed) {
        Map<String, String> timezoneData = SharedUtil.seedToTimezone(seed);
        return timezoneRepository.save(Timezone.builder()
                .GMT(timezoneData.get("GMT"))
                .name(timezoneData.get("name"))
                .build());
    }

    public boolean httpRefsExistByIds(List<Long> ids) {
        return !httpRefRepository.findAllById(ids).isEmpty();
    }

    public boolean exercisesExistByIds(List<Long> ids) {
        return !exerciseRepository.findAllById(ids).isEmpty();
    }

    public boolean workoutsExistByIds(List<Long> ids) {
        return !workoutRepository.findAllById(ids).isEmpty();
    }

    public boolean userExistsById(long userId) {
        return userRepository.findById(userId).isPresent();
    }

    public void saveUserChanges(User user) {
        userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.getReferenceById(id);
    }

    public BodyPart getBodyPartById(long id) {
        return bodyPartRepository.findById(id).orElse(null);
    }

    public HttpRef getHttpRefById(long id) {
        return httpRefRepository.findById(id).orElse(null);
    }

    public Exercise getExerciseById(long id) {
        return exerciseRepository.findById(id).orElse(null);
    }

    public Workout getWorkoutById(long id) {
        return workoutRepository.findById(id).orElse(null);
    }

    @Override
    public MentalActivity createDefaultMentalActivity(int seed, List<HttpRef> httpRefs, MentalType mentalType) {
        return this.createMentalActivityBase(seed, false, httpRefs, null, mentalType);
    }

    @Override
    public MentalActivity createCustomMentalActivity(
            int seed, List<HttpRef> httpRefs, MentalType mentalType, User user) {
        MentalActivity mental = this.createMentalActivityBase(seed, true, httpRefs, user, mentalType);
        if (user.getMentalActivities() == null) user.setMentalActivities(new HashSet<>());
        user.getMentalActivities().add(mental);
        userRepository.save(user);
        return mental;
    }

    private MentalActivity createMentalActivityBase(
            int seed, boolean isCustom, List<HttpRef> httpRefs, User user, MentalType mentalType) {
        MentalActivity mental = MentalActivity.builder()
                .title("Mental " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .user(user)
                .httpRefs(new HashSet<>(httpRefs))
                .type(mentalType)
                .build();
        return mentalRepository.save(mental);
    }

    @Override
    public MentalType createMeditationType() {
        return this.createMentalTypeBase("MEDITATION");
    }

    @Override
    public MentalType createAffirmationType() {
        return this.createMentalTypeBase("AFFIRMATION");
    }

    private MentalType createMentalTypeBase(String mentalType) {
        return mentalTypeRepository.save(MentalType.builder().name(mentalType).build());
    }

    public MentalActivity getMentalActivityById(long id) {
        return mentalRepository.findById(id).orElse(null);
    }

    public MentalWorkout getMentalWorkoutById(long id) {
        return mentalWorkoutRepository.findById(id).orElse(null);
    }

    @Override
    public MentalWorkout createCustomMentalWorkout(int seed, List<MentalActivity> mentalActivities, User user) {
        MentalWorkout mentalWorkout = this.createMentalWorkoutBase(seed, true, mentalActivities, user);
        if (user.getMentalWorkouts() == null) user.setMentalWorkouts(new HashSet<>());
        user.getMentalWorkouts().add(mentalWorkout);
        userRepository.save(user);
        return mentalWorkout;
    }

    @Override
    public MentalWorkout createDefaultMentalWorkout(int seed, List<MentalActivity> mentalActivities) {
        return this.createMentalWorkoutBase(seed, false, mentalActivities, null);
    }

    private MentalWorkout createMentalWorkoutBase(
            int seed, boolean isCustom, List<MentalActivity> mentalActivities, User user) {
        MentalWorkout mentalWorkout = MentalWorkout.builder()
                .title("MentalWorkout " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .mentalActivities(new HashSet<>(mentalActivities))
                .build();
        return mentalWorkoutRepository.save(mentalWorkout);
    }

    @Override
    public Nutrition createDefaultNutrition(int seed, List<HttpRef> httpRefs, NutritionType nutritionType) {
        return this.createNutritionBase(seed, false, httpRefs, null, nutritionType);
    }

    @Override
    public Nutrition createCustomNutrition(int seed, List<HttpRef> httpRefs, NutritionType nutritionType, User user) {
        Nutrition nutrition = this.createNutritionBase(seed, true, httpRefs, user, nutritionType);
        if (user.getNutritions() == null) user.setNutritions(new HashSet<>());
        user.getNutritions().add(nutrition);
        userRepository.save(user);
        return nutrition;
    }

    private Nutrition createNutritionBase(
            int seed, boolean isCustom, List<HttpRef> httpRefs, User user, NutritionType nutritionType) {

        Nutrition nutrition = Nutrition.builder()
                .title("Nutrition " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .httpRefs(new HashSet<>(httpRefs))
                .type(nutritionType)
                .build();

        return nutritionRepository.save(nutrition);
    }

    @Override
    public NutritionType createSupplementType() {
        return this.createNutritionTypeBase("SUPPLEMENT");
    }

    @Override
    public NutritionType createRecipeType() {
        return this.createNutritionTypeBase("RECIPE");
    }

    private NutritionType createNutritionTypeBase(String nutritionType) {
        return nutritionTypeRepository.save(
                NutritionType.builder().name(nutritionType).build());
    }
}
