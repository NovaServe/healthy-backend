package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.user.dto.CountryResponseDto;
import java.util.List;

public interface CountryService {
    List<CountryResponseDto> getCountries();
}
