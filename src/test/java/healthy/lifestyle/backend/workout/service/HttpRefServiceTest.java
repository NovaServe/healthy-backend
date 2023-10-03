package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Spy
    ModelMapper modelMapper;

    DataUtil dataUtil = new DataUtil();

    @Test
    void getHttpRefsTest_shouldReturnDefaultAndCustomRefs() {
        // Given
        List<HttpRef> httpRefsDefault = dataUtil.createHttpRefs(1, 2, false);
        List<HttpRef> httpRefsCustom = dataUtil.createHttpRefs(3, 4, true);
        List<HttpRef> httpRefs = new ArrayList<>(httpRefsDefault);
        httpRefs.addAll(httpRefsCustom);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        long userId = 1L;
        when(httpRefRepository.findAllDefault(sort)).thenReturn(httpRefsDefault);
        when(httpRefRepository.findCustomByUserId(userId, sort)).thenReturn(httpRefsCustom);

        // When
        List<HttpRefResponseDto> httpRefsActual = httpRefService.getHttpRefs(userId, sort, false);

        // Then
        verify(httpRefRepository, times(1)).findAllDefault(sort);
        verify(httpRefRepository, times(1)).findCustomByUserId(userId, sort);

        org.hamcrest.MatcherAssert.assertThat(httpRefsActual, hasSize(httpRefs.size()));

        assertThat(httpRefs)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(httpRefsActual);
    }

    @Test
    void getHttpRefsTest_shouldReturnDefaultOnlyRefs() {
        // When
        List<HttpRef> httpRefs = dataUtil.createHttpRefs(1, 2, false);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        long userId = 1L;
        when(httpRefRepository.findAllDefault(sort)).thenReturn(httpRefs);

        // When
        List<HttpRefResponseDto> httpRefsActual = httpRefService.getHttpRefs(userId, sort, true);

        // Then
        verify(httpRefRepository, times(1)).findAllDefault(sort);
        verify(httpRefRepository, times(0)).findCustomByUserId(userId, sort);

        org.hamcrest.MatcherAssert.assertThat(httpRefsActual, hasSize(httpRefs.size()));

        Assertions.assertThat(httpRefs)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(httpRefsActual);
    }

    @Test
    void getHttpRefsTest_shouldThrowException_whenNoRefs() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        long userId = 1L;
        when(httpRefRepository.findAllDefault(sort)).thenReturn(new ArrayList<>());
        when(httpRefRepository.findCustomByUserId(userId, sort)).thenReturn(new ArrayList<>());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> httpRefService.getHttpRefs(userId, sort, false));

        // Then
        verify(httpRefRepository, times(1)).findAllDefault(sort);
        verify(httpRefRepository, times(1)).findCustomByUserId(userId, sort);

        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }
}
