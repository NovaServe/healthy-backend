package healthy.lifestyle.backend.util;

import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
import healthy.lifestyle.backend.workout.dto.*;
import java.util.Collections;
import java.util.List;

public class DtoUtil {
    public HttpRefCreateRequestDto httpRefCreateRequestDto(int seed) {
        return HttpRefCreateRequestDto.builder()
                .name("HttpRef " + seed)
                .description("Desc " + seed)
                .ref("http://ref-" + seed)
                .build();
    }

    public HttpRefUpdateRequestDto httpRefUpdateRequestDto(int seed) {
        return HttpRefUpdateRequestDto.builder()
                .name("Update Name " + seed)
                .ref("https://update-ref-" + seed)
                .description("Update Desc " + seed)
                .build();
    }

    public HttpRefUpdateRequestDto httpRefUpdateRequestDtoEmpty() {
        return HttpRefUpdateRequestDto.builder().build();
    }

    public ExerciseCreateRequestDto exerciseCreateRequestDto(
            int seed, boolean needsEquipment, List<Long> bodyPartIds, List<Long> httpRefIds) {

        return ExerciseCreateRequestDto.builder()
                .title("Title " + seed)
                .description("Desc " + seed)
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
                .description("Desc " + seed)
                .exerciseIds(exerciseIds)
                .build();
    }

    public WorkoutUpdateRequestDto workoutUpdateRequestDto(int seed, List<Long> exerciseIds) {
        return WorkoutUpdateRequestDto.builder()
                .title("Update Workout " + seed)
                .description("Update Desc " + seed)
                .exerciseIds(exerciseIds)
                .build();
    }

    public WorkoutUpdateRequestDto workoutUpdateRequestDtoEmpty() {
        return WorkoutUpdateRequestDto.builder()
                .exerciseIds(Collections.emptyList())
                .build();
    }

    public SignupRequestDto signupRequestDto(int seed, Long countryId, Integer age) {
        return this.signupRequestDtoBase(seed, countryId, age);
    }

    public SignupRequestDto signupRequestDto(int seed, Long countryId) {
        return this.signupRequestDtoBase(seed, countryId, null);
    }

    private SignupRequestDto signupRequestDtoBase(int seed, Long countryId, Integer age) {
        int AGE_CONST = 20;
        return SignupRequestDto.builder()
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .password("Password-" + seed)
                .confirmPassword("Password-" + seed)
                .fullName("Full Name " + Shared.numberToText(seed))
                .countryId(countryId)
                .age(age == null ? AGE_CONST + seed : age)
                .build();
    }

    public LoginRequestDto loginRequestDto(int seed) {
        return new LoginRequestDto.Builder()
                .usernameOrEmail("email-" + seed + "@email.com")
                .password("Password-" + seed)
                .confirmPassword("Password-" + seed)
                .build();
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
}
