package healthy.lifestyle.backend.data;

import static org.assertj.core.api.Assertions.assertThat;

import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TestUtilities {
    public static void assertBodyPartsResponseDtoList(List<BodyPartResponseDto> actual, List<BodyPart> expected) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    public static void assertHttpRefsResponseDtoList(List<HttpRefResponseDto> actual, List<HttpRef> expected) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    public static void assertBodyParts(Set<BodyPart> actual, List<BodyPart> expected) {
        List<BodyPart> actualList = actual.stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
        assertThat(actualList)
                .usingRecursiveComparison()
                .ignoringFields("exercises")
                .isEqualTo(expected);
    }

    public static void assertHttpRefs(Set<HttpRef> actual, List<HttpRef> expected) {
        List<HttpRef> actualList =
                actual.stream().sorted(Comparator.comparingLong(HttpRef::getId)).toList();
        assertThat(actualList)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(expected);
    }
}
