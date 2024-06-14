package healthy.lifestyle.backend.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.activity.mental.dto.MentalCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.workout.dto.*;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.shared.util.JsonDescription;
import healthy.lifestyle.backend.user.dto.LoginRequestDto;
import healthy.lifestyle.backend.user.dto.SignupRequestDto;
import healthy.lifestyle.backend.user.dto.UserUpdateRequestDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class DtoUtil {

    public HttpRefCreateRequestDto httpRefCreateRequestDto(int seed) {
        return HttpRefCreateRequestDto.builder()
                .name("HttpRef " + seed)
                .description("Description " + seed)
                .ref("https://ref-" + seed + ".com")
                .httpRefType(HttpRefTypeEnum.YOUTUBE.name())
                .build();
    }

    public HttpRefCreateRequestDto httpRefCreateRequestDtoEmpty() {
        return HttpRefCreateRequestDto.builder().build();
    }

    public HttpRefUpdateRequestDto httpRefUpdateRequestDto(int seed) {
        return HttpRefUpdateRequestDto.builder()
                .name("Update Name " + seed)
                .ref("https://update-ref-" + seed + ".com")
                .httpRefType(HttpRefTypeEnum.YOUTUBE.name())
                .description("Update Description " + seed)
                .build();
    }

    public HttpRefUpdateRequestDto httpRefUpdateRequestDtoEmpty() {
        return HttpRefUpdateRequestDto.builder().build();
    }

    public ExerciseCreateRequestDto exerciseCreateRequestDto(
            int seed, boolean needsEquipment, List<Long> bodyPartIds, List<Long> httpRefIds) {

        return ExerciseCreateRequestDto.builder()
                .title("Title " + seed)
                .description("Description " + seed)
                .needsEquipment(needsEquipment)
                .bodyParts(bodyPartIds)
                .httpRefs(httpRefIds)
                .build();
    }

    public ExerciseUpdateRequestDto exerciseUpdateRequestDto(
            int seed, boolean needsEquipment, List<Long> bodyPartIds, List<Long> httpRefIds) {
        return ExerciseUpdateRequestDto.builder()
                .title("Updated Title-" + seed)
                .description("Updated Description-" + seed)
                .needsEquipment(needsEquipment)
                .bodyPartIds(bodyPartIds)
                .httpRefIds(httpRefIds)
                .build();
    }

    public ExerciseUpdateRequestDto exerciseUpdateRequestDtoEmpty() {
        return ExerciseUpdateRequestDto.builder().build();
    }

    public WorkoutCreateRequestDto workoutCreateRequestDto(int seed, List<Long> exerciseIds) {
        return WorkoutCreateRequestDto.builder()
                .title("Workout " + seed)
                .description("Description " + seed)
                .exerciseIds(exerciseIds)
                .build();
    }

    public WorkoutUpdateRequestDto workoutUpdateRequestDto(int seed, List<Long> exerciseIds) {
        return WorkoutUpdateRequestDto.builder()
                .title("Update Workout " + seed)
                .description("Update Description " + seed)
                .exerciseIds(exerciseIds)
                .build();
    }

    public WorkoutUpdateRequestDto workoutUpdateRequestDtoEmpty() {
        return WorkoutUpdateRequestDto.builder()
                .exerciseIds(Collections.emptyList())
                .build();
    }

    public SignupRequestDto signupRequestDto(int seed, Long countryId, Integer age, long timezoneId) {
        return this.signupRequestDtoBase(seed, countryId, age, timezoneId);
    }

    public SignupRequestDto signupRequestDto(int seed, Long countryId, long timezoneId) {
        return this.signupRequestDtoBase(seed, countryId, null, timezoneId);
    }

    public SignupRequestDto signupRequestDtoEmpty() {
        return SignupRequestDto.builder().build();
    }

    private SignupRequestDto signupRequestDtoBase(int seed, Long countryId, Integer age, long timezoneId) {
        int AGE_CONST = 20;
        return SignupRequestDto.builder()
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .password("Password-" + seed)
                .confirmPassword("Password-" + seed)
                .fullName("Full Name " + SharedUtil.numberToText(seed))
                .countryId(countryId)
                .age(age == null ? AGE_CONST + seed : age)
                .timezoneId(timezoneId)
                .build();
    }

    public LoginRequestDto loginRequestDto(int seed) {
        return LoginRequestDto.builder()
                .usernameOrEmail("email-" + seed + "@email.com")
                .password("Password-" + seed)
                .build();
    }

    public LoginRequestDto loginRequestDtoEmpty() {
        return LoginRequestDto.builder().build();
    }

    public UserUpdateRequestDto userUpdateRequestDto(String seed, Long countryId, Integer age) {
        return UserUpdateRequestDto.builder()
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .password("Password-" + seed)
                .confirmPassword("Password-" + seed)
                .fullName("Full Name " + seed)
                .countryId(countryId)
                .age(age)
                .build();
    }

    public UserUpdateRequestDto userUpdateRequestDtoEmpty() {
        return UserUpdateRequestDto.builder().build();
    }

    public MentalActivityUpdateRequestDto mentalActivityUpdateRequestDto(
            int seed, List<Long> httpRefIds, Long mentalTypeId) {
        return MentalActivityUpdateRequestDto.builder()
                .title("Updated Title-" + seed)
                .description("Updated Description-" + seed)
                .httpRefIds(httpRefIds)
                .mentalTypeId(mentalTypeId)
                .build();
    }

    public MentalActivityUpdateRequestDto mentalActivityUpdateRequestDtoEmpty() {
        return MentalActivityUpdateRequestDto.builder().build();
    }

    public MentalActivityCreateRequestDto mentalActivityCreateRequestDto(
            int seed, List<Long> httpRefIds, Long mentalTypeId) {
        return MentalActivityCreateRequestDto.builder()
                .title("Title-" + seed)
                .description("Description-" + seed)
                .httpRefs(httpRefIds)
                .mentalTypeId(mentalTypeId)
                .build();
    }

    public MentalWorkoutCreateRequestDto mentalWorkoutCreateRequestDto(int seed, List<Long> mentalActivityIds) {
        return MentalWorkoutCreateRequestDto.builder()
                .title("MentalWorkout " + seed)
                .description("Description " + seed)
                .mentalActivityIds(mentalActivityIds)
                .build();
    }

    public WorkoutPlanCreateRequestDto workoutPlanCreateRequestDto(
            Long workoutId, LocalDate startDate, LocalDate endDate, String jsonDescription) {
        return WorkoutPlanCreateRequestDto.builder()
                .workoutId(workoutId)
                .startDate(startDate)
                .endDate(endDate)
                .jsonDescription(jsonDescription)
                .build();
    }

    public WorkoutPlanCreateRequestDto workoutPlanCreateRequestDto(int seed, Long workoutId) {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);

        JsonDescription jsonDescription = JsonDescription.builder()
                .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                .hours(seed % 24)
                .minutes(seed % 60)
                .build();

        List<JsonDescription> jsonDescriptions = List.of(jsonDescription);

        String jsonDescriptionStringified;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonDescriptionStringified = objectMapper.writeValueAsString(jsonDescriptions);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return WorkoutPlanCreateRequestDto.builder()
                .workoutId(workoutId)
                .startDate(startDate)
                .endDate(endDate)
                .jsonDescription(jsonDescriptionStringified)
                .build();
    }
}
