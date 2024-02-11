package healthy.lifestyle.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.user.dto.CountryResponseDto;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.repository.CountryRepository;
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
public class CountryServiceTest {
    @InjectMocks
    CountryServiceImpl countryService;

    @Mock
    private CountryRepository countryRepository;

    @Spy
    ModelMapper modelMapper;

    TestUtil testUtil = new TestUtil();

    @Test
    public void getCountries_shouldReturnDtoList_whenValidRequest() {
        // Given
        Country country1 = testUtil.createCountry(1);
        Country country2 = testUtil.createCountry(2);
        List<Country> countries = List.of(country1, country2);
        when(countryRepository.findAll()).thenReturn(countries);

        // When
        List<CountryResponseDto> countryResponseDto = countryService.getCountries();

        // Then
        verify(countryRepository, times(1)).findAll();

        assertEquals(2, countryResponseDto.size());

        assertThat(countryResponseDto)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(countries);
    }
}
