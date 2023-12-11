package healthy.lifestyle.backend.workout.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.user.UserTestBuilder;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class ExerciseDeleteServiceTest {
    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserServiceImpl userService;

    @Spy
    ModelMapper modelMapper;

    @InjectMocks
    ExerciseServiceImpl exerciseService;

    UserTestBuilder userTestBuilder = new UserTestBuilder();

    @Test
    void deleteCustomExerciseTest_shouldReturnDeletedId_whenValidRequestGiven() {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExercises(2)
                .setAmountOfExerciseNestedEntities(4)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddMultipleExercises();

        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdFromSortedList(0), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(userWrapper.getExerciseFromSortedList(0)));
        doNothing().when(userService).deleteUserExercise(eq(userWrapper.getUserId()), any(Exercise.class));
        doNothing().when(exerciseRepository).delete(any(Exercise.class));

        // When
        Long deletedExerciseId = exerciseService.deleteCustomExercise(
                userWrapper.getExerciseIdFromSortedList(0), userWrapper.getUserId());

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseIdFromSortedList(0), userWrapper.getUserId());
        verify(userService, times(1)).deleteUserExercise(eq(userWrapper.getUserId()), any(Exercise.class));
        verify(exerciseRepository, times(1)).delete(any(Exercise.class));
        assertEquals(userWrapper.getExerciseIdFromSortedList(0), deletedExerciseId);
    }

    @Test
    void deleteCustomExerciseTest_shouldThrowNotFoundAnd404_whenExerciseNotFound() {
        // Given
        long wrongExerciseId = 1000L;
        long wrongUserId = 1000L;
        when(exerciseRepository.findCustomByExerciseIdAndUserId(wrongExerciseId, wrongUserId))
                .thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> exerciseService.deleteCustomExercise(wrongExerciseId, wrongUserId));

        // Then
        verify(exerciseRepository, times(1)).findCustomByExerciseIdAndUserId(wrongExerciseId, wrongUserId);
        verify(exerciseRepository, times(0)).delete(any(Exercise.class));
        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
    }
}
