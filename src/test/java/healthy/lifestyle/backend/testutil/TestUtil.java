package healthy.lifestyle.backend.testutil;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.nutrition.model.Nutrition;
import healthy.lifestyle.backend.activity.nutrition.model.NutritionType;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefTypeEnum;
import healthy.lifestyle.backend.activity.workout.model.*;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUtil implements Util {
    private final ModelMapper modelMapper = new ModelMapper();

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public BodyPart createBodyPart(int seed) {
        return BodyPart.builder().id((long) seed).name("Body part " + seed).build();
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
        return httpRef;
    }

    private HttpRef createHttpRefBase(int seed, boolean isCustom, HttpRefTypeEnum httpRefTypeEnum, User user) {
        HttpRefType httpRefType = HttpRefType.builder()
                .id((long) seed)
                .name(httpRefTypeEnum.name())
                .build();
        return HttpRef.builder()
                .id((long) seed)
                .name("Name " + seed)
                .ref("Ref " + seed)
                .httpRefType(httpRefType)
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
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createUser(int seed, int age) {
        Role role = Role.builder().id((long) seed).name("ROLE_USER").build();
        Country country =
                Country.builder().id((long) seed).name("Country-" + seed).build();
        return this.createUserBase(seed, role, country, age, null, null, null);
    }

    @Override
    public User createAdminUser(int seed) {
        Role role = Role.builder().name("ROLE_ADMIN").build();
        Country country = Country.builder().name("Country-" + seed).build();
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createUser(int seed, Role role, Country country) {
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createUser(int seed, Role role, Country country, int age) {
        return this.createUserBase(seed, role, country, age, null, null, null);
    }

    private User createUserBase(
            int seed,
            Role role,
            Country country,
            Integer age,
            List<HttpRef> httpRefs,
            List<Exercise> exercises,
            List<Workout> workouts) {
        int AGE_CONST = 20;
        return User.builder()
                .id((long) seed)
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .fullName("Full Name " + Shared.numberToText(seed))
                .role(role)
                .country(country)
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
}
