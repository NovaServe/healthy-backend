package healthy.lifestyle.backend.testutil;

import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.nutrition.model.Nutrition;
import healthy.lifestyle.backend.activity.nutrition.model.NutritionType;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import java.util.List;

public interface Util {
    BodyPart createBodyPart(int seed);

    HttpRef createDefaultHttpRef(int seed);

    HttpRef createCustomHttpRef(int seed, User user);

    Exercise createDefaultExercise(int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs);

    Exercise createCustomExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs, User user);

    Workout createDefaultWorkout(int seed, List<Exercise> exercises);

    Workout createCustomWorkout(int seed, List<Exercise> exercises, User user);

    WorkoutPlan createWorkoutPlan(Long seed, User user, Workout workout);

    User createUser(int seed);

    User createUser(int seed, int age);

    User createAdminUser(int seed);

    User createUser(int seed, Role role, Country country, Timezone timezone);

    User createUser(int seed, Role role, Country country, int age, Timezone timezone);

    Role createUserRole();

    Role createAdminRole();

    Country createCountry(int seed);

    Timezone createTimezone();

    Timezone createTimezone(int seed);

    Mental createDefaultMental(int seed, List<HttpRef> httpRefs, MentalType mentalType);

    Mental createCustomMental(int seed, List<HttpRef> httpRefs, MentalType mentalType, User user);

    MentalType createMeditationType();

    MentalType createAffirmationType();

    Nutrition createDefaultNutrition(int seed, List<HttpRef> httpRefs, NutritionType nutritionType);

    Nutrition createCustomNutrition(int seed, List<HttpRef> httpRefs, NutritionType nutritionType, User user);

    NutritionType createSupplementType();

    NutritionType createRecipeType();
}
