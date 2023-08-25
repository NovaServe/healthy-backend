package healthy.lifestyle.backend.workout.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * @see BodyPartServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class BodyPartServiceImplTest {
    @InjectMocks
    BodyPartServiceImpl bodyPartService;

    @Mock
    BodyPartRepository bodyPartRepository;

    @Test
    void getBodyPartsPositive() {
        BodyPart bodyPartStub1 =
                new BodyPart.Builder().id(1L).name("Body part 1").build();
        BodyPart bodyPartStub2 =
                new BodyPart.Builder().id(2L).name("Body part 2").build();
        BodyPart bodyPartStub3 =
                new BodyPart.Builder().id(3L).name("Body part 3").build();
        List<BodyPart> bodyPartsStub = List.of(bodyPartStub1, bodyPartStub2, bodyPartStub3);
        when(bodyPartRepository.findAll()).thenReturn(bodyPartsStub);

        List<BodyPartResponseDto> responseDto = bodyPartService.getBodyParts();
        assertEquals(bodyPartsStub.size(), responseDto.size());
        for (int i = 0; i < responseDto.size(); i++) {
            assertEquals(bodyPartsStub.get(i).getId(), responseDto.get(i).getId());
            assertEquals(bodyPartsStub.get(i).getName(), responseDto.get(i).getName());
        }
        verify(bodyPartRepository, times(1)).findAll();
    }

    @Test
    void getBodyPartsNegativeEmptyRepositoryNull() {
        when(bodyPartRepository.findAll()).thenReturn(null);
        ApiException exception = assertThrows(ApiException.class, () -> bodyPartService.getBodyParts());
        verify(bodyPartRepository, times(1)).findAll();
        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void getBodyPartsNegativeEmptyRepositoryEmptyList() {
        List<BodyPart> bodyPartStub = new ArrayList<>();
        when(bodyPartRepository.findAll()).thenReturn(bodyPartStub);
        ApiException exception = assertThrows(ApiException.class, () -> bodyPartService.getBodyParts());
        verify(bodyPartRepository, times(1)).findAll();
        assertEquals(ErrorMessage.SERVER_ERROR.getName(), exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }
}
