package healthy.lifestyle.backend.activity.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.activity.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefUpdateRequestDto;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.activity.workout.repository.HttpRefTypeRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class HttpRefServiceTest {
    @InjectMocks
    HttpRefServiceImpl httpRefService;

    @Mock
    HttpRefRepository httpRefRepository;

    @Mock
    UserService userService;

    @Mock
    HttpRefTypeRepository httpRefTypeRepository;

    @Spy
    ModelMapper modelMapper;

    @Spy
    VerificationUtil verificationUtil;

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();

    @Test
    void createCustomHttpRef_shouldReturnCreatedDto() {
        // Given
        User user = testUtil.createUser(1);
        HttpRefCreateRequestDto requestDto = dtoUtil.httpRefCreateRequestDto(1);
        HttpRef httpRef = testUtil.createDefaultHttpRef(1);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(httpRefTypeRepository.findByName(any(String.class)))
                .thenReturn(Optional.ofNullable(httpRef.getHttpRefType()));
        when(httpRefRepository.findDefaultAndCustomByNameAndUserId(requestDto.getName(), user.getId()))
                .thenReturn(Collections.emptyList());
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
        verify(httpRefRepository, times(1)).findDefaultAndCustomByNameAndUserId(requestDto.getName(), user.getId());
        verify(httpRefRepository, times(1)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "isCustom", "httpRefTypeName")
                .isEqualTo(requestDto);
        assertTrue(responseDto.isCustom());
        assertEquals(1L, responseDto.getId());
    }

    @Test
    void createCustomHttpRef_shouldThrowErrorWith404_whenInvalidUserId() {
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
    void createCustomHttpRef_shouldThrowErrorWith400_whenAlreadyExists() {
        // Given
        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(httpRefRepository.findDefaultAndCustomByNameAndUserId(createHttpRequestDto.getName(), user.getId()))
                .thenReturn(List.of(httpRef));

        // When
        ApiException actualException = assertThrows(
                ApiException.class, () -> httpRefService.createCustomHttpRef(user.getId(), createHttpRequestDto));

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(httpRefRepository, times(1))
                .findDefaultAndCustomByNameAndUserId(createHttpRequestDto.getName(), user.getId());
        verify(httpRefRepository, times(0)).save(org.mockito.ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }

    @Test
    void getCustomHttpRefById_shouldReturnDto_whenValidId() {
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
                .ignoringFields("exercises", "user", "httpRefTypeName")
                .isEqualTo(httpRef);
    }

    @Test
    void getCustomHttpRefById_shouldThrowErrorWith404_whenNotFound() {
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
    void getCustomHttpRefById_shouldThrowErrorAnd400_whenDefaultHttpRefRequested() {
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
    void getCustomHttpRefById_shouldThrowErrorWith400_whenHttpRefUserMismatch() {
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

    @ParameterizedTest
    @MethodSource("getHttpRefsValidFilters")
    void getHttpRefsWithFilter_shouldReturnDtoList_whenValidFilters(
            Boolean isCustom,
            Long userId,
            String name,
            String description,
            int totalElements,
            int totalPages,
            int numberOfElements,
            List<Long> resultSeeds) {
        // Given
        User user = testUtil.createUser(1);
        List<HttpRef> httpRefs = Arrays.asList(testUtil.createDefaultHttpRef(1), testUtil.createCustomHttpRef(2, user));
        List<HttpRef> filteredHttpRefs = httpRefs.stream()
                .filter(httpRef -> resultSeeds.contains(httpRef.getId()))
                .toList();

        if (userId != null) userId = user.getId();
        int currentPageNumber = 0;
        int itemsPerPage = 2;
        String orderBy = "ASC";
        String sortBy = "id";
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(orderBy), sortBy));
        Page<HttpRef> mockHttpRefPage = new PageImpl<>(filteredHttpRefs, pageable, totalElements);
        if (isCustom != null)
            when(httpRefRepository.findDefaultOrCustomWithFilter(isCustom, userId, name, description, pageable))
                    .thenReturn(mockHttpRefPage);
        else
            when(httpRefRepository.findDefaultAndCustomWithFilter(userId, name, description, pageable))
                    .thenReturn(mockHttpRefPage);

        // When
        Page<HttpRefResponseDto> httpRefPage = httpRefService.getHttpRefsWithFilter(
                isCustom, userId, name, description, sortBy, orderBy, currentPageNumber, itemsPerPage);

        // Then
        if (isCustom != null)
            verify(httpRefRepository, times(1))
                    .findDefaultOrCustomWithFilter(eq(isCustom), eq(userId), eq(name), eq(description), any());
        else
            verify(httpRefRepository, times(1))
                    .findDefaultAndCustomWithFilter(eq(userId), eq(name), eq(description), any());
        verify(modelMapper, times(resultSeeds.size())).map(any(), eq(HttpRefResponseDto.class));

        assertEquals(totalElements, httpRefPage.getTotalElements());
        assertEquals(totalPages, httpRefPage.getTotalPages());
        assertEquals(numberOfElements, httpRefPage.getNumberOfElements());
        assertEquals(numberOfElements, httpRefPage.getContent().size());
        assertEquals(currentPageNumber, httpRefPage.getNumber());
    }

    static Stream<Arguments> getHttpRefsValidFilters() {
        return Stream.of(
                Arguments.of(false, null, "Name", "Desc", 1, 1, 1, List.of(1L)),
                Arguments.of(true, 0L, "Name", "Desc", 1, 1, 1, List.of(2L)),
                Arguments.of(null, 0L, "Name", "Desc", 2, 1, 2, List.of(1L, 2L)));
    }

    @Test
    void updateCustomHttpRef_shouldReturnUpdatedDto_whenValidFields()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        User user = testUtil.createUser(1);
        HttpRef httpRef = testUtil.createCustomHttpRef(1, user);
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(1);

        when(httpRefRepository.findById(httpRef.getId())).thenReturn(Optional.of(httpRef));
        when(httpRefRepository.save(httpRef)).thenReturn(httpRef);
        when(httpRefTypeRepository.findByName(any(String.class)))
                .thenReturn(Optional.ofNullable(httpRef.getHttpRefType()));

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
    void updateCustomHttpRef_shouldThrowErrorWith404_whenNotFound() {
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
    void updateCustomHttpRef_shouldReturnErrorMessageAnd400_whenDefaultHttpRefRequested() {
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
    void updateCustomHttpRef_shouldThrowErrorWith400_whenHttpRefUserMismatch() {
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
    void updateCustomHttpRef_shouldThrowErrorWith400_whenEmptyRequest() {
        // Given
        User user = testUtil.createUser(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDtoEmpty();
        when(httpRefRepository.findById(anyLong())).thenReturn(Optional.ofNullable(customHttpRef));

        ApiExceptionCustomMessage expectedException =
                new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);

        // When
        ApiExceptionCustomMessage actualException = assertThrows(
                ApiExceptionCustomMessage.class,
                () -> httpRefService.updateCustomHttpRef(user.getId(), customHttpRef.getId(), requestDto));

        // Then
        verify(httpRefRepository, times(1)).findById(anyLong());
        verify(httpRefRepository, times(0)).save(ArgumentMatchers.any(HttpRef.class));

        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void deleteCustomHttpRef_shouldReturnVoid_whenValidId() {
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
    void deleteCustomHttpRef_shouldThrowErrorWith404_whenNotFound() {
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
    void deleteCustomHttpRef_shouldThrowErrorWith400_whenDefaultHttpRefRequested() {
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
    void deleteCustomHttpRef_shouldThrowErrorWith400_whenHttpRefUserMismatch() {
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
