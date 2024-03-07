package healthy.lifestyle.backend.admin.workout.service;

import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.activity.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.admin.workout.repository.ExerciseAdminRepository;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.model.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    @MethodSource("getExercisesValidFilters")
    void getExercisesWithFilter_shouldReturnDtoList_whenValidFilters(
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

        List<Exercise> expectedExercises = exercises.stream()
                .filter(exercise -> resultSeeds.contains(
                        Integer.parseInt(exercise.getTitle().split(" ")[1])))
                .collect(Collectors.toList());

        when(exerciseAdminRepository.findWithFilter(eq(title), eq(description), eq(isCustom), eq(needsEquipment)))
                .thenReturn(Optional.of(expectedExercises));

        // When
        List<ExerciseResponseDto> result =
                exerciseAdminService.getExercisesWithFilter(title, description, isCustom, needsEquipment);

        // Then
        for (int i = 0; i < resultSeeds.size(); i++) {
            Assertions.assertEquals(
                    "Exercise " + resultSeeds.get(i), result.get(i).getTitle());
            Assertions.assertEquals(
                    "Description " + resultSeeds.get(i), result.get(i).getDescription());
            Assertions.assertEquals(
                    expectedExercises.get(i).isCustom(), result.get(i).isCustom());
            Assertions.assertEquals(
                    expectedExercises.get(i).isNeedsEquipment(), result.get(i).isNeedsEquipment());
        }

        verify(exerciseAdminRepository, times(1))
                .findWithFilter(eq(title), eq(description), eq(isCustom), eq(needsEquipment));
    }

    static Stream<Arguments> getExercisesValidFilters() {
        return Stream.of(
                // Positive cases for default exercises
                Arguments.of(null, null, false, null, List.of(1, 2)),
                Arguments.of("Exercise 1", null, false, null, List.of(1)),
                Arguments.of("Exercise 2", "Description 2", false, false, List.of(2)),

                // Negative cases for default exercises
                Arguments.of("NonExistentValue", "NonExistentValue", false, null, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, false, true, Collections.emptyList()),

                // Positive cases for custom exercises
                Arguments.of(null, null, true, null, List.of(3, 4)),
                Arguments.of("Exercise 3", null, true, true, List.of(3)),
                Arguments.of("Exercise 4", "Description 4", true, false, List.of(4)),

                // Negative cases for custom exercises
                Arguments.of("NonExistentValue", "NonExistentValue", true, null, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, true, null, Collections.emptyList()),

                // Positive cases for all exercises
                Arguments.of(null, null, null, null, List.of(1, 2, 3, 4)),

                // Negative cases for all exercises
                Arguments.of("NonExistentValue", "NonExistentValue", null, null, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, null, null, Collections.emptyList()));
    }
}
