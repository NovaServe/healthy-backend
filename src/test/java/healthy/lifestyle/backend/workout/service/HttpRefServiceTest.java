package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
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

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();

    @Test
    void createCustomHttpRefTest_shouldReturnHttpRefResponseDto() {
        // Given
        User user = testUtil.createUser(1);
        HttpRefCreateRequestDto requestDto = dtoUtil.httpRefCreateRequestDto(1);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(httpRefRepository.findCustomByNameAndUserId(requestDto.getName(), user.getId()))
                .thenReturn(Optional.empty());
        when(httpRefRepository.save(org.mockito.ArgumentMatchers.any(HttpRef.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    HttpRef saved = (HttpRef) args[0];
                    saved.setId(1L);
                    return saved;
                });

        // When
        HttpRefResponseDto responseDto = httpRefService.createCustomHttpRef(user.getId(), requestDto);

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(httpRefRepository, times(1)).findCustomByNameAndUserId(requestDto.getName(), user.getId());
        verify(httpRefRepository, times(1)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "isCustom")
                .isEqualTo(requestDto);
        assertTrue(responseDto.isCustom());
        assertEquals(1L, responseDto.getId());
    }

    @Test
    void createCustomHttpRefTest_shouldThrowErrorWith404_whenInvalidUserIdProvided() {
        // Given
        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        long nonExistentUserId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_NOT_FOUND, nonExistentUserId, HttpStatus.NOT_FOUND);
        when(userService.getUserById(nonExistentUserId)).thenReturn(null);

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.createCustomHttpRef(nonExistentUserId, createHttpRequestDto));

        // Then
        verify(userService, times(1)).getUserById(nonExistentUserId);
        verify(httpRefRepository, times(0))
                .findCustomByNameAndUserId(createHttpRequestDto.getName(), nonExistentUserId);
        verify(httpRefRepository, times(0)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void createCustomHttpRefTest_shouldThrowErrorWith400_whenAlreadyExists() {
        // Given
        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(httpRefRepository.findCustomByNameAndUserId(createHttpRequestDto.getName(), user.getId()))
                .thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.createCustomHttpRef(user.getId(), createHttpRequestDto));

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(httpRefRepository, times(1)).findCustomByNameAndUserId(createHttpRequestDto.getName(), user.getId());
        verify(httpRefRepository, times(0)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getDefaultHttpRefsTest_shouldReturnDefaultHttpRefResponseDtoList() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        HttpRef httpRef1 = testUtil.createDefaultHttpRef(1);
        HttpRef httpRef2 = testUtil.createDefaultHttpRef(2);
        List<HttpRef> httpRefsDefault = List.of(httpRef1, httpRef2);
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
    void getCustomHttpRefByIdTest_shouldReturnHttpRefResponseDto() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        HttpRefResponseDto responseDto = httpRefService.getCustomHttpRefById(user.getId(), httpRef.getId());

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(httpRef);
    }

    @Test
    void getCustomHttpRefByIdTest_shouldThrowErrorWith404_whenHttpRefNotFound() {
        // Given
        long randomUserId = 1000L;
        long nonExistentHttpRefId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);
        when(httpRefRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.getCustomHttpRefById(randomUserId, nonExistentHttpRefId));

        // Then
        verify(httpRefRepository, times(1)).findById(anyLong());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getCustomHttpRefByIdTest_shouldThrowErrorAnd400_whenDefaultHttpRefRequestedInsteadOfCustom() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createDefaultHttpRef(1);
        ApiException expectedException = new ApiException(
                ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.getCustomHttpRefById(user.getId(), httpRef.getId()));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getCustomHttpRefByIdTest_shouldThrowErrorWith400_whenHttpRefDoesntBelongToUser() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        long anotherUserId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRef.getId(), HttpStatus.BAD_REQUEST);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.getCustomHttpRefById(anotherUserId, httpRef.getId()));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getCustomHttpRefsTest_shouldReturnCustomHttpRefResponseDtoList() {
        // Given
        HttpRef httpRef1 = testUtil.createDefaultHttpRef(1);
        HttpRef httpRef2 = testUtil.createDefaultHttpRef(2);
        HttpRef httpRef3 = testUtil.createDefaultHttpRef(3);
        HttpRef httpRef4 = testUtil.createDefaultHttpRef(4);
        List<HttpRef> httpRefs = List.of(httpRef1, httpRef2, httpRef3, httpRef4);
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
    void updateCustomHttpRefTest_shouldReturnUpdatedHttpRefResponseDto() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(1);

        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));
        when(httpRefRepository.save(httpRef)).thenReturn(httpRef);

        // When
        HttpRefResponseDto responseDto = httpRefService.updateCustomHttpRef(user.getId(), httpRef.getId(), requestDto);

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
        verify(httpRefRepository, times(1)).save(httpRef);

        assertEquals(requestDto.getName(), responseDto.getName());
        assertEquals(requestDto.getDescription(), responseDto.getDescription());
        assertEquals(requestDto.getRef(), responseDto.getRef());
        assertEquals(httpRef.getId(), responseDto.getId());
    }

    @Test
    void updateCustomHttpRefTest_shouldThrowErrorWith404_whenHttpRefNotFound() {
        // Given
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(1);
        long randomUserId = 1000L;
        long nonExistentHttpRefId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);
        when(httpRefRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException actualException = assertThrows(
                ApiException.class,
                () -> httpRefService.updateCustomHttpRef(randomUserId, nonExistentHttpRefId, requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(anyLong());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void updateCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenDefaultHttpRefRequestedInsteadOfCustom() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createDefaultHttpRef(1);
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(1);
        ApiException expectedException =
                new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class,
                () -> httpRefService.updateCustomHttpRef(user.getId(), httpRef.getId(), requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void updateCustomHttpRefTest_shouldThrowErrorWith400_whenHttpRefDoesntBelongToUser() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(1);
        long anotherUserId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRef.getId(), HttpStatus.BAD_REQUEST);

        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class,
                () -> httpRefService.updateCustomHttpRef(anotherUserId, httpRef.getId(), requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void updateCustomHttpRefTest_shouldThrowErrorWith400_whenEmptyRequest() {
        // Given
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDtoEmpty();
        long randomUserId = 1000L;
        long randomHttpRefId = 1000L;
        ApiException expectedException = new ApiException(ErrorMessage.EMPTY_REQUEST, null, HttpStatus.BAD_REQUEST);

        // When
        ApiException actualException = assertThrows(
                ApiException.class,
                () -> httpRefService.updateCustomHttpRef(randomUserId, randomHttpRefId, requestDto));

        // Then
        verify(httpRefRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void deleteCustomHttpRefTest_shouldReturnDeletedHttpRefId() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        httpRefService.deleteCustomHttpRef(user.getId(), httpRef.getId());

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());
    }

    @Test
    void deleteCustomHttpRefTest_shouldThrowErrorWith404_whenHttpRefNotFound() {
        // Given
        long randomUserId = 1000L;
        long nonExistentHttpRefId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);
        when(httpRefRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.deleteCustomHttpRef(randomUserId, nonExistentHttpRefId));

        // Then
        verify(httpRefRepository, times(1)).findById(anyLong());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void deleteCustomHttpRefTest_shouldThrowErrorWith400_whenDefaultHttpRefIsRequestedToDelete() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createDefaultHttpRef(1);
        ApiException expectedException =
                new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.deleteCustomHttpRef(user.getId(), httpRef.getId()));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void deleteCustomHttpRefTest_shouldThrowErrorWith400_whenHttpRefBelongsToAnotherUser() {
        // Given
        User user1 = testUtil.createUser(1);
        User user2 = testUtil.createUser(2);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user2);
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRef.getId(), HttpStatus.BAD_REQUEST);
        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.deleteCustomHttpRef(user1.getId(), httpRef.getId()));

        // Then
        verify(httpRefRepository, times(1)).findById(httpRef.getId());

        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }
}
