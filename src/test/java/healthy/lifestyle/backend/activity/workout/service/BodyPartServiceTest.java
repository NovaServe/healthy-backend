package healthy.lifestyle.backend.activity.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.activity.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.testutil.TestUtil;
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
    void getBodyParts_shouldReturnDtoList() {
        // Given
        BodyPart bodyPart1 = dataUtil.createBodyPart(1);
        BodyPart bodyPart2 = dataUtil.createBodyPart(2);
        List<BodyPart> bodyParts = List.of(bodyPart1, bodyPart2);
        when(bodyPartRepository.findAll()).thenReturn(bodyParts);

        // When
        List<BodyPartResponseDto> bodyPartsActual = bodyPartService.getBodyParts();

        // Then
        verify(bodyPartRepository, times(1)).findAll();
        assertEquals(2, bodyPartsActual.size());
        assertThat(bodyPartsActual)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(bodyParts);
    }

    @Test
    void getBodyParts_shouldThrowExceptionWith404_whenNotFound() {
        // Given
        ApiException expectedException = new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND);
        when(bodyPartRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        ApiException actualException = assertThrows(ApiException.class, () -> bodyPartService.getBodyParts());

        // Then
        verify(bodyPartRepository, times(1)).findAll();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
    }
}
