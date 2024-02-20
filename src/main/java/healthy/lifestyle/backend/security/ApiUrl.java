package healthy.lifestyle.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiUrl {
    @Value("/error/**")
    private String errorUrl;

    @Value("${api.basePath}/${api.version}/users/auth/**")
    private String authUrl;

    @Value("${api.basePath}/${api.version}/users")
    private String signupUrl;

    @Value("${api.basePath}/${api.version}/workouts/bodyParts")
    private String bodyPartsUrl;

    @Value("${api.basePath}/${api.version}/workouts/exercises/default")
    private String defaultExercisesUrl;

    @Value("${api.basePath}/${api.version}/users/countries")
    private String countriesUrl;

    @Value("${api.basePath}/${api.version}/workouts/exercises/default/{exercise_id}")
    private String defaultExerciseDetailsUrl;

    @Value("${api.basePath}/${api.version}/workouts/httpRefs/default")
    private String defaultHttpRefsUrl;

    @Value("${api.basePath}/${api.version}/workouts/default")
    private String defaultWorkoutsUrl;

    @Value("${api.basePath}/${api.version}/workouts/default/{workout_id}")
    private String defaultWorkoutDetailsUrl;

    @Value("${api.basePath}/${api.version}/nutritions/default/{nutrition_id}")
    private String defaultNutritionDetailsUrl;

    @Value("${api.basePath}/${api.version}/mentals/default/{mental_id}")
    private String defaultMentalDetailsUrl;

    @Value("${api.basePath}/${api.version}/mentals/default")
    private String defaultMentalsUrl;

    @Value("${api.basePath}/${api.version}/mentals/mental_type")
    private String mentalTypeUrl;
}
