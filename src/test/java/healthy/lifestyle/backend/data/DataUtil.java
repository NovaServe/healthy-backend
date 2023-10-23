package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.workout.dto.BodyPartRequestDto;
import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefRequestDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.HashSet;
import java.util.List;
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
            long id, boolean isCustom, boolean isCustomRefs, int start1, int end1, int start2, int end2) {
        List<HttpRef> httpRefs = createHttpRefs(start1, end1, isCustomRefs);
        List<BodyPart> bodyParts = createBodyParts(start2, end2);
        return Exercise.builder()
                .id(id)
                .title("Title " + id)
                .description("Desc " + id)
                .bodyParts(new HashSet<>(bodyParts))
                .httpRefs(new HashSet<>(httpRefs))
                .isCustom(isCustom)
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

    public CreateExerciseRequestDto createExerciseRequestDto(int seed, int start1, int end1, int start2, int end2) {
        List<HttpRefRequestDto> httpRefs = createHttpRefsRequestDto(start1, end1);
        List<BodyPartRequestDto> bodyParts = createBodyPartsRequestDto(start2, end2);
        return new CreateExerciseRequestDto.Builder()
                .title("Title " + seed)
                .description("Desc " + seed)
                .bodyParts(bodyParts)
                .httpRefs(httpRefs)
                .build();
    }

    public CreateExerciseRequestDto createExerciseRequestDto(
            int seed, List<BodyPart> bodyParts, List<HttpRef> httpRefs) {
        List<BodyPartRequestDto> bodyPartRequestDtoList = bodyParts.stream()
                .map(elt -> modelMapper.map(elt, BodyPartRequestDto.class))
                .toList();

        List<HttpRefRequestDto> httpRefRequestDtoList = httpRefs.stream()
                .map(elt -> modelMapper.map(elt, HttpRefRequestDto.class))
                .toList();

        return new CreateExerciseRequestDto.Builder()
                .title("Title " + seed)
                .description("Desc " + seed)
                .bodyParts(bodyPartRequestDtoList)
                .httpRefs(httpRefRequestDtoList)
                .build();
    }

    public SignupRequestDto createSignupRequestDto(String seed, Long countryId) {
        return new SignupRequestDto.Builder()
                .username("username-" + seed)
                .email("username-" + seed + "@email.com")
                .password("password-" + seed)
                .confirmPassword("password-" + seed)
                .fullName("Full Name " + seed)
                .country(countryId)
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
}
