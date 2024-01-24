package healthy.lifestyle.backend.nutritions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.nutrition.dto.NutritionResponseDto;
import healthy.lifestyle.backend.nutrition.model.Nutrition;
import healthy.lifestyle.backend.nutrition.model.NutritionType;
import healthy.lifestyle.backend.nutrition.repository.NutritionRepository;
import healthy.lifestyle.backend.nutrition.repository.NutritionTypeRepository;
import healthy.lifestyle.backend.nutrition.service.NutritionServiceImpl;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.Comparator;
import java.util.List;
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
public class NutritionServiceTest {
    @Mock
    private NutritionRepository nutritionRepository;

    @Mock
    private NutritionTypeRepository nutritionTypeRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @Mock
    private UserServiceImpl userService;

    @Spy
    ModelMapper modelMapper;

    @InjectMocks
    NutritionServiceImpl nutritionService;

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();

    @Test
    void getDefaultNutritionByIdTest_shouldReturnDefaultNutritionDto() {
        // Given
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        NutritionType nutritionType1 = testUtil.createSupplementType(1);
        NutritionType nutritionType2 = testUtil.createRecipeType(2);

        Nutrition defaultNutrition = testUtil.createDefaultNutrition(1, List.of(defaultHttpRef), nutritionType1);

        when(nutritionRepository.findById(defaultNutrition.getId())).thenReturn(Optional.of(defaultNutrition));

        // When
        NutritionResponseDto nutritionDtoActual =
                nutritionService.getNutritionById(defaultNutrition.getId(), true, null);

        // Then
        verify(nutritionRepository, times(1)).findById(defaultNutrition.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertThat(defaultNutrition)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "type")
                .isEqualTo(nutritionDtoActual);

        List<HttpRef> httpRefs = defaultNutrition.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();

        assertThat(httpRefs)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals", "nutritions")
                .isEqualTo(nutritionDtoActual.getHttpRefs());
    }

    @Test
    void getDefaultNutritionByIdTest_shouldThrowErrorWith404_whenNutritionNotFound() {
        // Given
        long nonExistingNutritionId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.NUTRITION_NOT_FOUND, nonExistingNutritionId, HttpStatus.NOT_FOUND);

        when(nutritionRepository.findById(nonExistingNutritionId)).thenReturn(Optional.empty());

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> nutritionService.getNutritionById(nonExistingNutritionId, true, null));

        // Then
        verify(nutritionRepository, times(1)).findById(nonExistingNutritionId);
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getDefaultNutritionByIdTest_shouldThrowErrorWith400_whenDefaultNutritionRequestedInsteadOfCustom() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        NutritionType nutritionType = testUtil.createSupplementType(1);

        Nutrition customNutrition = testUtil.createCustomNutrition(1, List.of(defaultHttpRef), nutritionType, user);
        ApiException expectedException = new ApiException(
                ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        when(nutritionRepository.findById(customNutrition.getId())).thenReturn(Optional.of(customNutrition));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> nutritionService.getNutritionById(customNutrition.getId(), true, null));

        // Then
        verify((nutritionRepository), times(1)).findById(customNutrition.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getDefaultNutritionByIdTest_shouldThrowErrorWith400_whenRequestedNutritionDoesntBelongToUser() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        NutritionType nutritionType = testUtil.createSupplementType(1);
        Nutrition customNutrition =
                testUtil.createCustomNutrition(1, List.of(defaultHttpRef, customHttpRef), nutritionType, user);

        User user2 = testUtil.createUser(2);

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_NUTRITION_MISMATCH, customNutrition.getId(), HttpStatus.BAD_REQUEST);

        when(nutritionRepository.findById(customNutrition.getId())).thenReturn(Optional.of(customNutrition));
        when(userService.getUserById(user2.getId())).thenReturn(user2);

        // When
        ApiException actualException = assertThrows(
                ApiException.class,
                () -> nutritionService.getNutritionById(customNutrition.getId(), false, user2.getId()));

        // Then
        verify((nutritionRepository), times(1)).findById(customNutrition.getId());
        verify(userService, times(1)).getUserById(user2.getId());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }
}
