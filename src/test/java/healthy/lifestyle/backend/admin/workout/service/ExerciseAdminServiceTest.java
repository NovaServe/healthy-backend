package healthy.lifestyle.backend.admin.workout.service;

import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.admin.workout.repository.ExerciseAdminRepository;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class ExerciseAdminServiceTest {

    @InjectMocks
    ExerciseAdminServiceImpl exerciseAdminService;

    @Mock
    private ExerciseAdminRepository exerciseAdminRepository;

    @Spy
    private TestUtil testUtil;

    @Spy
    ModelMapper modelMapper;

    @ParameterizedTest
    @MethodSource("multipleFilters")
    void getExercisesByFiltersTest_shouldReturnEntityResponseDtoList_whenParamsAreValid(
            String title, String description, Boolean isCustom, Boolean needsEquipment, List<Integer> resultSeeds) {
        // Given
        User user1 = testUtil.createUser(1);
        User user2 = testUtil.createUser(2);

        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        BodyPart bodyPart3 = testUtil.createBodyPart(3);
        BodyPart bodyPart4 = testUtil.createBodyPart(4);

        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = testUtil.createDefaultHttpRef(2);

        HttpRef customHttpRef1 = testUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = testUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef3 = testUtil.createCustomHttpRef(5, user2);
        HttpRef customHttpRef4 = testUtil.createCustomHttpRef(6, user2);

        Exercise defaultExercise1 =
                testUtil.createDefaultExercise(1, true, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 =
                testUtil.createDefaultExercise(2, false, List.of(bodyPart2), List.of(defaultHttpRef2));

        Exercise customExercise1 = testUtil.createCustomExercise(
                3, true, List.of(bodyPart3), List.of(customHttpRef1, customHttpRef2), user1);
        Exercise customExercise2 = testUtil.createCustomExercise(
                4, false, List.of(bodyPart4), List.of(customHttpRef3, customHttpRef4), user2);

        List<Exercise> exercises = Arrays.asList(defaultExercise1, defaultExercise2, customExercise1, customExercise2);

        List<Exercise> expectedExercises = filterExercises(exercises, title, description, isCustom, needsEquipment);

        when(exerciseAdminRepository.findByFilters(eq(title), eq(description), eq(isCustom), eq(needsEquipment)))
                .thenReturn(Optional.of(expectedExercises));

        // When
        List<ExerciseResponseDto> result =
                exerciseAdminService.getExercisesByFilters(title, description, isCustom, needsEquipment);

        // Then
        Assertions.assertEquals(expectedExercises.size(), result.size());
        for (int i = 0; i < resultSeeds.size(); i++) {
            Assertions.assertEquals(
                    "Exercise " + resultSeeds.get(i), result.get(i).getTitle());
            Assertions.assertEquals("Desc " + resultSeeds.get(i), result.get(i).getDescription());
            Assertions.assertEquals(
                    expectedExercises.get(i).isCustom(), result.get(i).isCustom());
            Assertions.assertEquals(
                    expectedExercises.get(i).isNeedsEquipment(), result.get(i).isNeedsEquipment());
        }

        verify(exerciseAdminRepository, times(1))
                .findByFilters(eq(title), eq(description), eq(isCustom), eq(needsEquipment));
    }

    public List<Exercise> filterExercises(
            List<Exercise> exercises, String title, String description, Boolean isCustom, Boolean needsEquipment) {
        List<Exercise> filteredExercises = new ArrayList<>();

        for (Exercise exercise : exercises) {
            boolean includeExercise = true;

            if (title != null && !exercise.getTitle().equals(title)) {
                includeExercise = false;
            }
            if (description != null && !exercise.getDescription().equals(description)) {
                includeExercise = false;
            }
            if (isCustom != null && exercise.isCustom() != isCustom) {
                includeExercise = false;
            }
            if (needsEquipment != null && exercise.isNeedsEquipment() != needsEquipment) {
                includeExercise = false;
            }

            if (includeExercise) {
                filteredExercises.add(exercise);
            }
        }

        return filteredExercises;
    }

    static Stream<Arguments> multipleFilters() {
        return Stream.of(
                // Positive cases for default exercises
                Arguments.of(null, null, false, null, List.of(1, 2)),
                Arguments.of("Exercise 1", null, false, null, List.of(1)),
                Arguments.of("Exercise 2", "Desc 2", false, false, List.of(2)),

                // Negative cases for default exercises
                Arguments.of("NonExistentVale", "NonExistentVale", false, null, Collections.emptyList()),
                Arguments.of("NonExistentVale", null, false, true, Collections.emptyList()),

                // Positive cases for custom exercises
                Arguments.of(null, null, true, null, List.of(3, 4)),
                Arguments.of("Exercise 3", null, true, true, List.of(3)),
                Arguments.of("Exercise 4", "Desc 4", true, false, List.of(4)),

                // Negative cases for custom exercises
                Arguments.of("NonExistentVale", "NonExistentVale", true, null, Collections.emptyList()),
                Arguments.of("NonExistentVale", null, true, null, Collections.emptyList()),

                // Positive cases for all exercises
                Arguments.of(null, null, null, null, List.of(1, 2, 3, 4)),

                // Negative cases for all exercises
                Arguments.of("NonExistentVale", "NonExistentVale", null, null, Collections.emptyList()),
                Arguments.of("NonExistentVale", null, null, null, Collections.emptyList()));
    }
}
