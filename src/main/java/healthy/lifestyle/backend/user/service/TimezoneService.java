package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.user.dto.TimezoneResponseDto;
import java.util.List;

public interface TimezoneService {
    List<TimezoneResponseDto> getTimezones();
}
