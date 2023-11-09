package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.UpdateHttpRefRequestDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class HttpRefServiceTest {
    @InjectMocks
    HttpRefServiceImpl httpRefService;

    @Mock
    HttpRefRepository httpRefRepository;

    @Mock
    UserService userService;

    @Spy
    ModelMapper modelMapper;

    DataUtil dataUtil = new DataUtil();

    @Test
    void getCustomHttpRefsTest_shouldReturnCustomRefs_whenValidUserIdAndSortByProvided() {
        // Given
        List<HttpRef> httpRefs = dataUtil.createHttpRefs(1, 4, true);
        long userId = 1L;
        String sortBy = "id";
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);
        when(httpRefRepository.findCustomByUserId(userId, sort)).thenReturn(httpRefs);

        // When
        List<HttpRefResponseDto> httpRefsActual = httpRefService.getCustomHttpRefs(userId, "id");

        // Then
        verify(httpRefRepository, times(1)).findCustomByUserId(userId, sort);

        org.hamcrest.MatcherAssert.assertThat(httpRefsActual, hasSize(httpRefs.size()));

        assertThat(httpRefs)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(httpRefsActual);
    }

    @Test
    void getDefaultHttpRefsTest_shouldReturnDefaultHttpRefs() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<HttpRef> httpRefsDefault = dataUtil.createHttpRefs(1, 2, false);
        when(httpRefRepository.findAllDefault(sort)).thenReturn(httpRefsDefault);

        // When
        List<HttpRefResponseDto> httpRefsActual = httpRefService.getDefaultHttpRefs(sort);

        // Then
        verify(httpRefRepository, times(1)).findAllDefault(sort);

        org.hamcrest.MatcherAssert.assertThat(httpRefsActual, hasSize(httpRefsDefault.size()));

        Assertions.assertThat(httpRefsDefault)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(httpRefsActual);
    }

    @Test
    void createCustomHttpRefTest_shouldReturnHttpRefResponseDto() {
        // Given
        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        long userId = 1L;
        User user = dataUtil.createUserEntity(userId);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(httpRefRepository.findCustomByNameAndUserId(createHttpRequestDto.getName(), userId))
                .thenReturn(Optional.empty());
        when(httpRefRepository.save(org.mockito.ArgumentMatchers.any(HttpRef.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    HttpRef saved = (HttpRef) args[0];
                    saved.setId(1L);
                    return saved;
                });

        // When
        HttpRefResponseDto httpRefResponseDto = httpRefService.createCustomHttpRef(user.getId(), createHttpRequestDto);

        // Then
        verify(userService, times(1)).getUserById(userId);
        verify(httpRefRepository, times(1)).findCustomByNameAndUserId(createHttpRequestDto.getName(), userId);
        verify(httpRefRepository, times(1)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertThat(httpRefResponseDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "isCustom")
                .isEqualTo(createHttpRequestDto);

        assertTrue(httpRefResponseDto.isCustom());
        assertEquals(1L, httpRefResponseDto.getId());
    }

    @Test
    void createCustomHttpRefTest_shouldReturnUserNotFoundAndBadRequest_whenInvalidUserIdProvided() {
        // Given
        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(null);

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> httpRefService.createCustomHttpRef(userId, createHttpRequestDto));

        // Then
        verify(userService, times(1)).getUserById(userId);
        verify(httpRefRepository, times(0)).findCustomByNameAndUserId(createHttpRequestDto.getName(), userId);
        verify(httpRefRepository, times(0)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertEquals(ErrorMessage.USER_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void createCustomHttpRefTest_shouldReturnAlreadyExistsAndBadRequest_whenNameDuplicated() {
        // Given
        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        long userId = 1L;
        User user = dataUtil.createUserEntity(userId);
        when(userService.getUserById(user.getId())).thenReturn(user);

        HttpRef httpRef = dataUtil.createHttpRef(1, true, null);
        when(httpRefRepository.findCustomByNameAndUserId(createHttpRequestDto.getName(), userId))
                .thenReturn(Optional.of(httpRef));

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> httpRefService.createCustomHttpRef(user.getId(), createHttpRequestDto));

        // Then
        verify(userService, times(1)).getUserById(userId);
        verify(httpRefRepository, times(1)).findCustomByNameAndUserId(createHttpRequestDto.getName(), userId);
        verify(httpRefRepository, times(0)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertEquals(ErrorMessage.ALREADY_EXISTS.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomHttpRefTest_shouldReturnHttpRefResponseDto_whenValidUpdateDtoProvided() {
        // Given
        User user = dataUtil.createUserEntity(1);
        HttpRef httpRef = dataUtil.createHttpRef(1, true, user);
        UpdateHttpRefRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(1);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));
        when(httpRefRepository.save(httpRef)).thenReturn(httpRef);

        // When
        HttpRefResponseDto responseDto = httpRefService.updateCustomHttpRef(user.getId(), httpRef.getId(), requestDto);

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
        verify(httpRefRepository, times(1)).save(httpRef);

        assertEquals(requestDto.getUpdatedName(), responseDto.getName());
        assertEquals(requestDto.getUpdatedDescription(), responseDto.getDescription());
        assertEquals(requestDto.getUpdatedRef(), responseDto.getRef());
        assertEquals(httpRef.getId(), responseDto.getId());
    }

    @Test
    void updateCustomHttpRefTest_shouldReturnNotFoundAnd400_whenHttpRefNotFound() {
        // Given
        UpdateHttpRefRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(1);
        when(httpRefRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> httpRefService.updateCustomHttpRef(1L, 2L, requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(anyLong());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenDefaultMediaIsRequestedToUpdate() {
        // Given
        User user = dataUtil.createUserEntity(1);
        HttpRef httpRef = dataUtil.createHttpRef(1, false, user);
        UpdateHttpRefRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(1);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> httpRefService.updateCustomHttpRef(user.getId(), httpRef.getId(), requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenUserResourceMismatch() {
        // Given
        User user = dataUtil.createUserEntity(1);
        HttpRef httpRef = dataUtil.createHttpRef(1, true, user);
        UpdateHttpRefRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(1);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));
        long wrongUserId = user.getId() + 1;

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> httpRefService.updateCustomHttpRef(wrongUserId, httpRef.getId(), requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void deleteCustomHttpRefTest_shouldReturnDeletedHttpRefId() {
        // Given
        User user = dataUtil.createUserEntity(1);
        HttpRef httpRef = dataUtil.createHttpRef(1, true, user);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        long deletedId = httpRefService.deleteCustomHttpRef(user.getId(), httpRef.getId());

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(httpRef.getId(), deletedId);
    }

    @Test
    void deleteCustomHttpRefTest_shouldReturnNotFoundAnd400_whenHttpRefNotFound() {
        // Given
        when(httpRefRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> httpRefService.deleteCustomHttpRef(1L, 2L));

        // Then
        verify(httpRefRepository, times(1)).findById(anyLong());

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void deleteCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenDefaultMediaIsRequestedToDelete() {
        // Given
        User user = dataUtil.createUserEntity(1);
        HttpRef httpRef = dataUtil.createHttpRef(1, false, user);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> httpRefService.deleteCustomHttpRef(user.getId(), httpRef.getId()));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void deleteCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenUserResourceMismatch() {
        // Given
        User user = dataUtil.createUserEntity(1);
        HttpRef httpRef = dataUtil.createHttpRef(1, true, user);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));
        long wrongUserId = user.getId() + 1;

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> httpRefService.deleteCustomHttpRef(wrongUserId, httpRef.getId()));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }
}
