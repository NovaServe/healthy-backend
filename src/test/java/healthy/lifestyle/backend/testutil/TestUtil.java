package healthy.lifestyle.backend.testutil;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.nutrition.model.Nutrition;
import healthy.lifestyle.backend.activity.nutrition.model.NutritionType;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import java.time.*;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUtil implements Util {
    private final ModelMapper modelMapper = new ModelMapper();

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BodyPart createBodyPart(int seed) {
        return BodyPart.builder().id((long) seed).name("Body part " + seed).build();
    }

    @Override
    public HttpRef createDefaultHttpRef(int seed) {
        return this.createHttpRefBase(seed, false, null);
    }

    @Override
    public HttpRef createCustomHttpRef(int seed, User user) {
        HttpRef httpRef = this.createHttpRefBase(seed, true, user);
        if (user.getHttpRefs() == null) user.setHttpRefs(new HashSet<>());
        user.getHttpRefs().add(httpRef);
        return httpRef;
    }

    private HttpRef createHttpRefBase(int seed, boolean isCustom, User user) {
        return HttpRef.builder()
                .id((long) seed)
                .name("Name " + seed)
                .ref("Ref " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .build();
    }

    @Override
    public Exercise createDefaultExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs) {
        return this.createExerciseBase(seed, false, needsEquipment, bodyParts, httpRefs, null);
    }

    public Exercise createDefaultExercise(int seed) {
        BodyPart bodyPart = createBodyPart(seed);
        HttpRef httpRef = createDefaultHttpRef(seed);

        return Exercise.builder()
                .id(Long.valueOf(seed))
                .isCustom(false)
                .needsEquipment(false)
                .bodyParts(Set.of(bodyPart))
                .httpRefs(Set.of(httpRef))
                .user(null)
                .build();
    }

    @Override
    public Exercise createCustomExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs, User user) {
        Exercise exercise = this.createExerciseBase(seed, true, needsEquipment, bodyParts, httpRefs, user);
        if (user.getExercises() == null) user.setExercises(new HashSet<>());
        user.getExercises().add(exercise);
        return exercise;
    }

    private Exercise createExerciseBase(
            int seed,
            boolean isCustom,
            boolean needsEquipment,
            List<BodyPart> bodyParts,
            List<HttpRef> httpRefs,
            User user) {
        return Exercise.builder()
                .id((long) seed)
                .title("Exercise " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .needsEquipment(needsEquipment)
                .user(user)
                .bodyParts(new HashSet<>(bodyParts))
                .httpRefs(new HashSet<>(httpRefs))
                .build();
    }

    @Override
    public Workout createDefaultWorkout(int seed, List<Exercise> exercises) {
        return this.createWorkoutBase(seed, false, exercises, null);
    }

    public Workout createDefaultWorkout(int seed) {
        Exercise exercise = createDefaultExercise(seed);
        return this.createWorkoutBase(seed, false, List.of(exercise), null);
    }

    @Override
    public Workout createCustomWorkout(int seed, List<Exercise> exercises, User user) {
        Workout workout = this.createWorkoutBase(seed, true, exercises, user);
        if (user.getWorkouts() == null) user.setWorkouts(new HashSet<>());
        user.getWorkouts().add(workout);
        return workout;
    }

    private Workout createWorkoutBase(int seed, boolean isCustom, List<Exercise> exercises, User user) {
        return Workout.builder()
                .id((long) seed)
                .title("Workout " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .exercises(new HashSet<>(exercises))
                .build();
    }

    @Override
    public User createUser(int seed) {
        Role role = Role.builder().id((long) seed).name("ROLE_USER").build();
        Country country =
                Country.builder().id((long) seed).name("Country-" + seed).build();
        Timezone timezone = createTimezone(seed);
        return this.createUserBase(seed, role, country, null, timezone, null, null, null);
    }

    @Override
    public User createUser(int seed, int age) {
        Role role = Role.builder().id((long) seed).name("ROLE_USER").build();
        Country country =
                Country.builder().id((long) seed).name("Country-" + seed).build();
        Timezone timezone = createTimezone(seed);
        return this.createUserBase(seed, role, country, age, timezone, null, null, null);
    }

    @Override
    public User createAdminUser(int seed) {
        Role role = Role.builder().name("ROLE_ADMIN").build();
        Country country = Country.builder().name("Country-" + seed).build();
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
        return User.builder()
                .id((long) seed)
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .fullName("Full Name " + SharedUtil.numberToText(seed))
                .role(role)
                .country(country)
                .timezone(timezone)
                .age(age == null ? AGE_CONST + seed : age)
                .password(passwordEncoder.encode("Password-" + seed))
                .httpRefs(isNull(httpRefs) ? null : new HashSet<>(httpRefs))
                .exercises(isNull(exercises) ? null : new HashSet<>(exercises))
                .workouts(isNull(workouts) ? null : new HashSet<>(workouts))
                .build();
    }

    @Override
    public Country createCountry(int seed) {
        return Country.builder().id((long) seed).name("Country " + seed).build();
    }

    @Override
    public Timezone createTimezone() {
        return createTimezone(1);
    }

    @Override
    public Timezone createTimezone(int seed) {
        String[] zones = TimeZone.getAvailableIDs();
        int Id = seed % zones.length;

        ZoneId zoneId = ZoneId.of(zones[Id]);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        ZoneOffset offset = zonedDateTime.now().getOffset();

        return Timezone.builder()
                .id((long) seed)
                .GMT("GMT" + offset)
                .name(zones[Id])
                .build();
    }

    @Override
    public Role createUserRole() {
        return this.createRoleBase("USER", null);
    }

    public Role createUserRole(int id) {
        return this.createRoleBase("USER", (long) id);
    }

    @Override
    public Role createAdminRole() {
        return this.createRoleBase("ADMIN", null);
    }

    public Role createAdminRole(int id) {
        return this.createRoleBase("ADMIN", (long) id);
    }

    private Role createRoleBase(String role, Long id) {
        return Role.builder().id(id).name("ROLE_" + role).build();
    }

    @Override
    public Mental createDefaultMental(int seed, List<HttpRef> httpRefs, MentalType mentalType) {
        return this.createMentalBase(seed, false, httpRefs, null, mentalType);
    }

    @Override
    public Mental createCustomMental(int seed, List<HttpRef> httpRefs, MentalType mentalType, User user) {
        Mental mental = this.createMentalBase(seed, true, httpRefs, user, mentalType);
        if (user.getMentals() == null) user.setMentals(new HashSet<>());
        user.getMentals().add(mental);
        return mental;
    }

    public Mental createMentalBase(
            int seed, boolean isCustom, List<HttpRef> httpRefs, User user, MentalType mentalType) {
        return Mental.builder()
                .id((long) seed)
                .title("Mental " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .httpRefs(new HashSet<>(httpRefs))
                .type(mentalType)
                .build();
    }

    @Override
    public MentalType createMeditationType() {
        return this.createMentalTypeBase("MEDITATION", null);
    }

    public MentalType createMeditationType(int id) {
        return this.createMentalTypeBase("MEDITATION", (long) id);
    }

    @Override
    public MentalType createAffirmationType() {
        return this.createMentalTypeBase("AFFIRMATION", null);
    }

    public MentalType createAffirmationType(int id) {
        return this.createMentalTypeBase("AFFIRMATION", (long) id);
    }

    private MentalType createMentalTypeBase(String mentalType, Long id) {
        return MentalType.builder().id(id).name(mentalType).build();
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
        return nutrition;
    }

    private Nutrition createNutritionBase(
            int seed, boolean isCustom, List<HttpRef> httpRefs, User user, NutritionType nutritionType) {

        return Nutrition.builder()
                .id((long) seed)
                .title("Nutrition " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .httpRefs(new HashSet<>(httpRefs))
                .type(nutritionType)
                .build();
    }

    @Override
    public NutritionType createSupplementType() {
        return this.createNutritionTypeBase(null, "SUPPLEMENT");
    }

    public NutritionType createSupplementType(long id) {
        return this.createNutritionTypeBase(id, "SUPPLEMENT");
    }

    @Override
    public NutritionType createRecipeType() {
        return this.createNutritionTypeBase(null, "RECIPE");
    }

    public NutritionType createRecipeType(long id) {
        return this.createNutritionTypeBase(id, "RECIPE");
    }

    private NutritionType createNutritionTypeBase(Long id, String nutritionType) {
        return NutritionType.builder().id(id).name(nutritionType).build();
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

        return WorkoutPlan.builder()
                .id(seed)
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .workout(workout)
                .jsonDescription(List.of(jsonDescription))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .deactivatedAt(null)
                .build();
    }
}
