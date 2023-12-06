package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.UpdateUserRequestDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.modelmapper.ModelMapper;

public class DataUtil {
    private final ModelMapper modelMapper = new ModelMapper();

    public List<BodyPart> createBodyParts(int start, int endInclusive) {
        return LongStream.rangeClosed(start, endInclusive)
                .mapToObj(
                        id -> BodyPart.builder().id(id).name("Body part " + id).build())
                .collect(Collectors.toList());
    }

    public HttpRef createHttpRef(int seed, boolean isCustom, User user) {
        return HttpRef.builder()
                .id((long) seed)
                .name("Name " + seed)
                .ref("https://ref " + seed)
                .description("Description " + seed)
                .isCustom(isCustom)
                .user(user)
                .build();
    }

    public UpdateHttpRefRequestDto createUpdateHttpRefRequestDto(int seed) {
        return UpdateHttpRefRequestDto.builder()
                .updatedName("Update Name " + seed)
                .updatedRef("https://ref-updated-" + seed)
                .updatedDescription("Updated Description " + seed)
                .build();
    }

    public List<HttpRef> createHttpRefs(int start, int endInclusive, boolean isCustom) {
        return LongStream.rangeClosed(start, endInclusive)
                .mapToObj(id -> HttpRef.builder()
                        .id(id)
                        .name("Title " + id)
                        .ref("Ref " + id)
                        .description("Desc " + id)
                        .isCustom(isCustom)
                        .build())
                .collect(Collectors.toList());
    }

    public Exercise createExercise(
            long id,
            boolean isCustom,
            boolean needsEquipment,
            boolean isCustomRefs,
            int start1,
            int end1,
            int start2,
            int end2) {
        List<HttpRef> httpRefs = createHttpRefs(start1, end1, isCustomRefs);
        List<BodyPart> bodyParts = createBodyParts(start2, end2);
        return Exercise.builder()
                .id(id)
                .title("Title " + id)
                .description("Desc " + id)
                .bodyParts(new HashSet<>(bodyParts))
                .httpRefs(new HashSet<>(httpRefs))
                .isCustom(isCustom)
                .needsEquipment(needsEquipment)
                .build();
    }

    public List<HttpRefRequestDto> createHttpRefsRequestDto(int start, int endInclusive) {
        return LongStream.rangeClosed(start, endInclusive)
                .mapToObj(id -> new HttpRefRequestDto.Builder().id(id).build())
                .collect(Collectors.toList());
    }

    public List<BodyPartRequestDto> createBodyPartsRequestDto(int start, int endInclusive) {
        return LongStream.rangeClosed(start, endInclusive)
                .mapToObj(id -> new BodyPartRequestDto.Builder().id(id).build())
                .collect(Collectors.toList());
    }

    public CreateExerciseRequestDto createExerciseRequestDto(
            int seed, boolean needsEquipment, Long[] bodyPartIds, Long[] httpRefIds) {

        return CreateExerciseRequestDto.builder()
                .title("Title " + seed)
                .description("Desc " + seed)
                .needsEquipment(needsEquipment)
                .bodyParts(Arrays.asList(bodyPartIds))
                .httpRefs(Arrays.asList(httpRefIds))
                .build();
    }

    public SignupRequestDto createSignupRequestDto(String seed, Long id, Integer age) {
        return SignupRequestDto.builder()
                .username("username-" + seed)
                .email("username-" + seed + "@email.com")
                .password("password-" + seed)
                .confirmPassword("password-" + seed)
                .fullName("Full Name " + seed)
                .countryId(id)
                .age(age)
                .build();
    }

    public LoginRequestDto createLoginRequestDto(String seed) {
        return new LoginRequestDto.Builder()
                .usernameOrEmail("username-" + seed + "@email.com")
                .password("password-" + seed)
                .confirmPassword("password-" + seed)
                .build();
    }

    public Country createCountry(int seed) {
        return Country.builder().name("Country " + seed).build();
    }

    public Workout createWorkout(long id, boolean isCustom, Set<Exercise> exercises) {
        return Workout.builder()
                .id(id)
                .title("Title " + id)
                .description("Description " + id)
                .isCustom(isCustom)
                .exercises(exercises)
                .build();
    }

    public CreateHttpRequestDto createHttpRequestDto(int seed) {
        return CreateHttpRequestDto.builder()
                .name("Name " + seed)
                .description("Description " + seed)
                .ref("http://ref-" + seed)
                .build();
    }

    public User createUserEntity(long userId) {
        return User.builder()
                .id(userId)
                .username("username-" + userId)
                .fullName("Full Name " + userId)
                .email("username-" + userId + "@email.com")
                .build();
    }

    public UpdateUserRequestDto createUpdateUserRequestDto(String seed, Long countryId, Integer age) {
        return UpdateUserRequestDto.builder()
                .username("username-" + seed)
                .email("username-" + seed + "@email.com")
                .password("password-" + seed)
                .confirmPassword("password-" + seed)
                .fullName("Full Name " + seed)
                .countryId(countryId)
                .updatedAge(age)
                .build();
    }

    public CreateWorkoutRequestDto createWorkoutRequestDto(int seed, List<Long> exerciseIds) {
        return CreateWorkoutRequestDto.builder()
                .title("Title-" + seed)
                .description("Description-" + seed)
                .exerciseIds(exerciseIds)
                .build();
    }

    public UpdateWorkoutRequestDto updateWorkoutRequestDto(int seed, List<Long> exerciseIds) {
        return UpdateWorkoutRequestDto.builder()
                .title("Title-" + seed)
                .description("Description-" + seed)
                .exerciseIds(exerciseIds)
                .build();
    }
}
