package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.BodyPartTestBuilder;
import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
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

    DataUtil dataUtil = new DataUtil();

    BodyPartTestBuilder bodyPartTestBuilder = new BodyPartTestBuilder();

    //    @Test
    //    void getBodyPartsTest_shouldReturnAllBodyParts() {
    //        // Given
    //        List<BodyPart> bodyParts = dataUtil.createBodyParts(1, 3);
    //        when(bodyPartRepository.findAll()).thenReturn(bodyParts);
    //
    //        // When
    //        List<BodyPartResponseDto> bodyPartsActual = bodyPartService.getBodyParts();
    //
    //        // Then
    //        verify(bodyPartRepository, times(1)).findAll();
    //        org.hamcrest.MatcherAssert.assertThat(bodyPartsActual, hasSize(bodyParts.size()));
    //        assertThat(bodyParts)
    //                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
    //                .isEqualTo(bodyPartsActual);
    //    }

    @Test
    void getBodyPartsTest_shouldReturnAllBodyParts() {
        // Given
        BodyPartTestBuilder.BodyPartWrapper bodyPartWrapper = bodyPartTestBuilder.getWrapper();
        bodyPartWrapper.setAmountOfEntities(2).setStartId(1).buildBodyPartsList();
        when(bodyPartRepository.findAll()).thenReturn(bodyPartWrapper.getEntities());

        // When
        List<BodyPartResponseDto> bodyPartsActual = bodyPartService.getBodyParts();

        // Then
        verify(bodyPartRepository, times(1)).findAll();
        org.hamcrest.MatcherAssert.assertThat(bodyPartsActual, hasSize(bodyPartWrapper.size()));
        assertThat(bodyPartWrapper.getEntities())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(bodyPartsActual);
    }

    @Test
    void getBodyPartsTest_shouldThrowException_whenNoRefs_null() {
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
    void getBodyPartsTest_shouldThrowException_whenNoRefs_emptyList() {
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
