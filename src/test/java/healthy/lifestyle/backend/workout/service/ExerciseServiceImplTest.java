package healthy.lifestyle.backend.workout.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.base.UnitBaseTest;
import healthy.lifestyle.backend.common.ValidationServiceImpl;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * @see ExerciseServiceImpl
 */
class ExerciseServiceImplTest extends UnitBaseTest {
    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private BodyPartRepository bodyPartRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @Mock
    private ValidationServiceImpl validationService;

    @InjectMocks
    ExerciseServiceImpl exerciseService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createExercise_Positive() {
        // Data
        BodyPartRequestDto bodyPartRequestDto1 =
                new BodyPartRequestDto.Builder().id(1L).build();
        BodyPartRequestDto bodyPartRequestDto2 =
                new BodyPartRequestDto.Builder().id(2L).build();

        HttpRefRequestDto httpRefRequestDto1 =
                new HttpRefRequestDto.Builder().id(1L).build();
        HttpRefRequestDto httpRefRequestDto2 =
                new HttpRefRequestDto.Builder().id(2L).build();
        HttpRefRequestDto httpRefRequestDto3 =
                new HttpRefRequestDto.Builder().id(3L).build();

        String exerciseTitle = "Narrow push-ups";
        String exerciseDescription = "Train triceps";

        CreateExerciseRequestDto requestDto = new CreateExerciseRequestDto(
                exerciseTitle,
                exerciseDescription,
                Set.of(bodyPartRequestDto1, bodyPartRequestDto2),
                Set.of(httpRefRequestDto1, httpRefRequestDto2, httpRefRequestDto3));

        long userId = 1L;

        BodyPart bodyPart1 = new BodyPart.Builder().id(1L).name("Arms").build();
        BodyPart bodyPart2 = new BodyPart.Builder().id(2L).name("Triceps").build();
        HttpRef httpRef1 = new HttpRef.Builder()
                .id(1L)
                .name("Media1")
                .ref("https://ref1.com")
                .description("Description1")
                .build();
        HttpRef httpRef2 = new HttpRef.Builder()
                .id(2L)
                .name("Media2")
                .ref("https://ref2.com")
                .description("Description2")
                .build();
        HttpRef httpRef3 = new HttpRef.Builder()
                .id(3L)
                .name("Media3")
                .ref("https://ref3.com")
                .build();
        Exercise exercise = new Exercise.Builder()
                .id(1L)
                .title(exerciseTitle)
                .description(exerciseDescription)
                .bodyParts(Set.of(bodyPart1, bodyPart2))
                .httpRefs(Set.of(httpRef1, httpRef2, httpRef3))
                .build();

        BodyPartResponseDto bodyPartResponseDto1 =
                new BodyPartResponseDto.Builder().id(1L).name("Arms").build();
        BodyPartResponseDto bodyPartResponseDto2 =
                new BodyPartResponseDto.Builder().id(2L).name("Triceps").build();
        HttpRefResponseDto httpRefResponseDto1 = new HttpRefResponseDto.Builder()
                .id(1L)
                .name("Media1")
                .ref("https://ref1.com")
                .description("Description1")
                .build();
        HttpRefResponseDto httpRefResponseDto2 = new HttpRefResponseDto.Builder()
                .id(2L)
                .name("Media2")
                .ref("https://ref2.com")
                .description("Description2")
                .build();
        HttpRefResponseDto httpRefResponseDto3 = new HttpRefResponseDto.Builder()
                .id(3L)
                .name("Media3")
                .ref("https://ref3.com")
                .build();

        // Stubs
        when(validationService.checkText(any(String.class))).thenReturn(true);

        when(bodyPartRepository.existsById(anyLong())).thenReturn(true);
        when(bodyPartRepository.getReferenceById(1L)).thenReturn(bodyPart1);
        when(bodyPartRepository.getReferenceById(2L)).thenReturn(bodyPart2);

        when(httpRefRepository.existsById(anyLong())).thenReturn(true);
        when(httpRefRepository.getReferenceById(1L)).thenReturn(httpRef1);
        when(httpRefRepository.getReferenceById(2L)).thenReturn(httpRef2);
        when(httpRefRepository.getReferenceById(3L)).thenReturn(httpRef3);

        when(exerciseRepository.findByTitleAndUserId(exerciseTitle, userId)).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exercise);

        // Test
        CreateExerciseResponseDto actual = exerciseService.createExercise(requestDto, userId);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getTitle(), actual.getTitle());
        assertEquals(requestDto.getDescription(), actual.getDescription());

        assertEquals(requestDto.getBodyParts().size(), actual.getBodyParts().size());
        List<BodyPartRequestDto> initialBodyParts = Arrays.asList(bodyPartRequestDto1, bodyPartRequestDto2);
        initialBodyParts.sort(Comparator.comparing(BodyPartRequestDto::getId));

        List<BodyPartResponseDto> actualBodyParts = new ArrayList<>(actual.getBodyParts());
        actualBodyParts.sort(Comparator.comparing(BodyPartResponseDto::getId));

        for (int i = 0; i < initialBodyParts.size(); i++) {
            assertEquals(initialBodyParts.get(i).getId(), actualBodyParts.get(i).getId());
            assertNotNull(actualBodyParts.get(i).getName());
        }

        assertEquals(requestDto.getHttpRefs().size(), actual.getHttpRefs().size());
        List<HttpRefRequestDto> initialHttpRefs =
                Arrays.asList(httpRefRequestDto1, httpRefRequestDto2, httpRefRequestDto3);
        initialHttpRefs.sort(Comparator.comparing(HttpRefRequestDto::getId));

        List<HttpRefResponseDto> actualHttpRefs = new ArrayList<HttpRefResponseDto>(actual.getHttpRefs());
        actualHttpRefs.sort(Comparator.comparing(HttpRefResponseDto::getId));

        for (int i = 0; i < initialHttpRefs.size(); i++) {
            assertEquals(initialHttpRefs.get(i).getId(), actualHttpRefs.get(i).getId());
            assertNotNull(actualHttpRefs.get(i).getRef());
            if (i == 0 || i == 1) {
                assertNotNull(actualHttpRefs.get(i).getDescription());
            }
        }
    }
}
