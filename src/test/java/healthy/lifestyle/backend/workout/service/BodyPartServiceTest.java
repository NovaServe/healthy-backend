package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class BodyPartServiceTest {
    @InjectMocks
    BodyPartServiceImpl bodyPartService;

    @Mock
    BodyPartRepository bodyPartRepository;

    @Spy
    ModelMapper modelMapper;

    TestUtil dataUtil = new TestUtil();

    @Test
    void getBodyPartsTest_shouldReturnAllBodyParts() {
        // Given
        BodyPart bodyPart1 = dataUtil.createBodyPart(1);
        BodyPart bodyPart2 = dataUtil.createBodyPart(2);
        BodyPart bodyPart3 = dataUtil.createBodyPart(3);
        List<BodyPart> bodyParts = List.of(bodyPart1, bodyPart2, bodyPart3);
        when(bodyPartRepository.findAll()).thenReturn(bodyParts);

        // When
        List<BodyPartResponseDto> bodyPartsActual = bodyPartService.getBodyParts();

        // Then
        verify(bodyPartRepository, times(1)).findAll();
        org.hamcrest.MatcherAssert.assertThat(bodyPartsActual, hasSize(bodyParts.size()));
        assertThat(bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(bodyPartsActual);
    }

    @Test
    void getBodyPartsTest_shouldThrowException_whenNullHttpRefs() {
        // Given
        when(bodyPartRepository.findAll()).thenReturn(null);

        // When
        ApiException exception = assertThrows(ApiException.class, () -> bodyPartService.getBodyParts());

        // Then
        verify(bodyPartRepository, times(1)).findAll();
        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void getBodyPartsTest_shouldThrowException_whenEmptyListHttpRefs() {
        // Given
        when(bodyPartRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> bodyPartService.getBodyParts());

        // Then
        verify(bodyPartRepository, times(1)).findAll();
        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }
}
