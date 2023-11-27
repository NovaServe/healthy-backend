package healthy.lifestyle.backend.data;

import static org.assertj.core.api.Assertions.assertThat;

import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;

public class TestUtilities {
    public static void assertBodyParts(List<BodyPartResponseDto> actual, List<BodyPart> expected) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    public static void assertHttpRefs(List<HttpRefResponseDto> actual, List<HttpRef> expected) {
        assertThat(actual).usingRecursiveComparison().ignoringFields().isEqualTo(expected);
    }
}
