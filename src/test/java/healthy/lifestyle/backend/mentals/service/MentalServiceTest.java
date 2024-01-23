package healthy.lifestyle.backend.mentals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.mentals.dto.MentalResponseDto;
import healthy.lifestyle.backend.mentals.model.Mental;
import healthy.lifestyle.backend.mentals.model.MentalType;
import healthy.lifestyle.backend.mentals.repository.MentalRepository;
import healthy.lifestyle.backend.mentals.repository.MentalTypeRepository;
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
class MentalServiceTest {
    @Mock
    private MentalRepository mentalRepository;

    @Mock
    private MentalTypeRepository mentalTypeRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @Mock
    private UserServiceImpl userService;

    @Spy
    ModelMapper modelMapper;

    @InjectMocks
    MentalServiceImpl mentalService;

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();

    @Test
    void getMentalByIdTest_shouldReturnDefaultMentalDto() {
        // Given
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        MentalType mentalType = testUtil.createAffirmationType();
        Mental defaultMental = testUtil.createDefaultMental(1, List.of(defaultHttpRef), mentalType);

        when(mentalRepository.findById(defaultMental.getId())).thenReturn(Optional.of(defaultMental));

        // When
        MentalResponseDto mentalDtoActual = mentalService.getMentalById(defaultMental.getId(), true, null);

        // Then
        verify((mentalRepository), times(1)).findById(defaultMental.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertThat(defaultMental)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "type")
                .isEqualTo(mentalDtoActual);

        List<HttpRef> httpRefs_ = defaultMental.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(httpRefs_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals", "nutritions")
                .isEqualTo(mentalDtoActual.getHttpRefs());
    }

    @Test
    void getMentalByIdTest_shouldThrowErrorWith404_whenMentalNotFound() {
        // Given
        long nonExistingMentalId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistingMentalId, HttpStatus.NOT_FOUND);
        when(mentalRepository.findById(nonExistingMentalId)).thenReturn(Optional.empty());

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> mentalService.getMentalById(nonExistingMentalId, true, null));

        // Then
        verify((mentalRepository), times(1)).findById(nonExistingMentalId);
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getMentalByIdTest_shouldThrowErrorWith400_whenDefaultMentalRequestedInsteadOfCustom() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        MentalType mentalType = testUtil.createAffirmationType(1);

        Mental customMental = testUtil.createCustomMental(1, List.of(defaultHttpRef, customHttpRef), mentalType, user);
        ApiException expectedException = new ApiException(
                ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        when(mentalRepository.findById(customMental.getId())).thenReturn(Optional.of(customMental));

        // When
        ApiException actualException =
                assertThrows(ApiException.class, () -> mentalService.getMentalById(customMental.getId(), true, null));

        // Then
        verify((mentalRepository), times(1)).findById(customMental.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getMentalByIdTest_shouldThrowErrorWith400_whenRequestedMentalDoesntBelongToUser() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        MentalType mentalType = testUtil.createAffirmationType(1);
        Mental customMental = testUtil.createCustomMental(1, List.of(defaultHttpRef, customHttpRef), mentalType, user);

        User user2 = testUtil.createUser(2);

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, customMental.getId(), HttpStatus.BAD_REQUEST);

        when(mentalRepository.findById(customMental.getId())).thenReturn(Optional.of(customMental));
        when(userService.getUserById(user2.getId())).thenReturn(user2);

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> mentalService.getMentalById(customMental.getId(), false, user2.getId()));

        // Then
        verify((mentalRepository), times(1)).findById(customMental.getId());
        verify(userService, times(1)).getUserById(user2.getId());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }
}
