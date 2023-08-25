package healthy.lifestyle.backend.workout.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

/**
 * @see HttpRefServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class HttpRefServiceImplTest {
    @InjectMocks
    HttpRefServiceImpl httpRefService;

    @Mock
    HttpRefRepository httpRefRepository;

    @Test
    void getHttpRefsPositive() {
        HttpRef httpRef1 = new HttpRef.Builder()
                .id(1L)
                .name("Name 1")
                .ref("Ref 1")
                .description("Desc 1")
                .isCustom(false)
                .build();
        HttpRef httpRef2 = new HttpRef.Builder()
                .id(2L)
                .name("Name 2")
                .ref("Ref 2")
                .description("Desc 2")
                .isCustom(true)
                .build();
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        long userId = 1L;
        when(httpRefRepository.findAllDefault(sort)).thenReturn(List.of(httpRef1));
        when(httpRefRepository.findByUserId(userId, sort)).thenReturn(List.of(httpRef2));

        List<HttpRefResponseDto> responseDtoList = httpRefService.getHttpRefs(userId, sort);
        verify(httpRefRepository, times(1)).findAllDefault(sort);
        verify(httpRefRepository, times(1)).findByUserId(userId, sort);
        assertEquals(2, responseDtoList.size());

        assertEquals(httpRef1.getId(), responseDtoList.get(0).getId());
        assertEquals(httpRef1.getName(), responseDtoList.get(0).getName());
        assertEquals(httpRef1.getRef(), responseDtoList.get(0).getRef());
        assertEquals(httpRef1.getDescription(), responseDtoList.get(0).getDescription());

        assertEquals(httpRef2.getId(), responseDtoList.get(1).getId());
        assertEquals(httpRef2.getName(), responseDtoList.get(1).getName());
        assertEquals(httpRef2.getRef(), responseDtoList.get(1).getRef());
        assertEquals(httpRef2.getDescription(), responseDtoList.get(1).getDescription());
    }

    @Test
    void getHttpRefsPositiveDefaultOnly() {
        HttpRef httpRef1 = new HttpRef.Builder()
                .id(1L)
                .name("Name 1")
                .ref("Ref 1")
                .description("Desc 1")
                .isCustom(false)
                .build();
        HttpRef httpRef2 = new HttpRef.Builder()
                .id(2L)
                .name("Name 2")
                .ref("Ref 2")
                .description("Desc 2")
                .isCustom(false)
                .build();
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        long userId = 1L;
        when(httpRefRepository.findAllDefault(sort)).thenReturn(List.of(httpRef1, httpRef2));
        when(httpRefRepository.findByUserId(userId, sort)).thenReturn(null);

        List<HttpRefResponseDto> responseDtoList = httpRefService.getHttpRefs(userId, sort);
        verify(httpRefRepository, times(1)).findAllDefault(sort);
        verify(httpRefRepository, times(1)).findByUserId(userId, sort);
        assertEquals(2, responseDtoList.size());

        assertEquals(httpRef1.getId(), responseDtoList.get(0).getId());
        assertEquals(httpRef1.getName(), responseDtoList.get(0).getName());
        assertEquals(httpRef1.getRef(), responseDtoList.get(0).getRef());
        assertEquals(httpRef1.getDescription(), responseDtoList.get(0).getDescription());

        assertEquals(httpRef2.getId(), responseDtoList.get(1).getId());
        assertEquals(httpRef2.getName(), responseDtoList.get(1).getName());
        assertEquals(httpRef2.getRef(), responseDtoList.get(1).getRef());
        assertEquals(httpRef2.getDescription(), responseDtoList.get(1).getDescription());
    }

    @Test
    void getHttpRefsNegativeEmpty() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        long userId = 1L;
        when(httpRefRepository.findAllDefault(sort)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> httpRefService.getHttpRefs(userId, sort));
        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        verify(httpRefRepository, times(1)).findAllDefault(sort);
        verify(httpRefRepository, times(0)).findByUserId(userId, sort);
    }
}
