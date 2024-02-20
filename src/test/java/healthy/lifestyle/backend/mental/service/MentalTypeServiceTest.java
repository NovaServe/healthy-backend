package healthy.lifestyle.backend.mental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.mental.dto.MentalTypeResponseDto;
import healthy.lifestyle.backend.mental.model.MentalType;
import healthy.lifestyle.backend.mental.repository.MentalTypeRepository;
import healthy.lifestyle.backend.util.TestUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class MentalTypeServiceTest {

    @InjectMocks
    MentalTypeServiceImpl mentalTypeService;

    @Mock
    MentalTypeRepository mentalTypeRepository;

    @Spy
    ModelMapper modelMapper;

    TestUtil testUtil = new TestUtil();

    @Test
    public void getMentalTypeTest_shouldReturnAllGetMentalType() {
        // Given
        MentalType mentalType1 = testUtil.createMentalType(1);
        MentalType mentalType2 = testUtil.createMentalType(2);
        MentalType mentalType3 = testUtil.createMentalType(3);
        List<MentalType> mentalTypeList = List.of(mentalType1, mentalType2, mentalType3);
        when(mentalTypeRepository.findAll()).thenReturn(mentalTypeList);

        // When
        List<MentalTypeResponseDto> mentalTypeResponseDto = mentalTypeService.getMentalType();

        // Then
        verify(mentalTypeRepository, times(1)).findAll();

        assertEquals(3, mentalTypeResponseDto.size());

        assertThat(mentalTypeResponseDto)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(mentalTypeList);
    }
}
