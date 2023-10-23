package healthy.lifestyle.backend.users.service;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import java.util.List;
import java.util.stream.IntStream;
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

    DataUtil dataUtil = new DataUtil();

    @Test
    public void getCountriesTest_shouldReturnAllCountries() {
        // Given
        List<Country> countries = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataUtil.createCountry(id))
                .toList();
        when(countryRepository.findAll()).thenReturn(countries);
        // When
        List<CountryResponseDto> countryResponseDto = countryService.getAllCountries();

        // Then
        verify(countryRepository, times(1)).findAll();
        org.hamcrest.MatcherAssert.assertThat(countryResponseDto, hasSize(countries.size()));
    }
}
