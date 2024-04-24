package healthy.lifestyle.backend.activity.mental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.mental.repository.MentalRepository;
import healthy.lifestyle.backend.activity.mental.repository.MentalTypeRepository;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserServiceImpl;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class MentalServiceTest {
    @InjectMocks
    MentalServiceImpl mentalService;

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

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();

    @Test
    void getMentalById_shouldReturnDefaultMentalDto_whenValidId() {
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
    void getMentalById_shouldThrowErrorWith404_whenNotFound() {
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
    void getMentalById_shouldThrowErrorWith400_whenMentalUserMismatch() {
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

    @Test
    void getMentalById_shouldReturnCustomMentalDto_whenValidId() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        MentalType mentalType = testUtil.createAffirmationType(1);
        Mental customMental = testUtil.createCustomMental(1, List.of(defaultHttpRef, customHttpRef), mentalType, user);

        when(mentalRepository.findById(customMental.getId())).thenReturn(Optional.of(customMental));
        when(userService.getUserById(user.getId())).thenReturn(user);

        // When
        MentalResponseDto mentalDtoActual = mentalService.getMentalById(customMental.getId(), false, user.getId());

        // Then
        verify((mentalRepository), times(1)).findById(customMental.getId());
        verify(userService, times(1)).getUserById(user.getId());

        Assertions.assertThat(customMental)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "type")
                .isEqualTo(mentalDtoActual);

        List<HttpRef> httpRefs_ = customMental.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(httpRefs_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals", "nutritions")
                .isEqualTo(mentalDtoActual.getHttpRefs());
    }

    @Test
    void getMentals_shouldReturnPageMentalsDto() {
        int currentPageNumber = 0;
        int itemsPerPage = 2;
        String orderBy = "ASC";
        String sortBy = "id";

        User user = testUtil.createUser(1);
        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = testUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = testUtil.createAffirmationType();
        MentalType mentalType2 = testUtil.createMeditationType();
        Mental defaultMental1 = testUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = testUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType1);
        Mental defaultMental3 = testUtil.createDefaultMental(3, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental4 = testUtil.createDefaultMental(4, List.of(defaultHttpRef1), mentalType2);

        List<Mental> filteredMentalList = Stream.of(defaultMental1, defaultMental2, defaultMental3, defaultMental4)
                .sorted(Comparator.comparingLong(Mental::getId))
                .toList();
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(orderBy), sortBy));

        Page<Mental> mockMentalPage = new PageImpl<>(filteredMentalList, pageable, filteredMentalList.size());

        when(mentalRepository.findDefaultAndCustomMentals(user.getId(), pageable))
                .thenReturn(mockMentalPage);

        // When
        Page<MentalResponseDto> dtoPage =
                mentalService.getMentals(user.getId(), sortBy, orderBy, currentPageNumber, itemsPerPage);

        // Then
        verify((mentalRepository), times(1)).findDefaultAndCustomMentals(user.getId(), pageable);

        assertThat(mockMentalPage)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "type")
                .isEqualTo(dtoPage);

        assertEquals(filteredMentalList.size(), dtoPage.getTotalElements());
        assertEquals(4, dtoPage.getContent().size());
    }
}
