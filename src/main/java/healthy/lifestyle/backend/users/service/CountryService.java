package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import java.util.List;

public interface CountryService {
    List<CountryResponseDto> getAllCountries();
}
