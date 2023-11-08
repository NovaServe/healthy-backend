package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface HttpRefService {
    List<HttpRefResponseDto> getDefaultHttpRefs(Sort sort);

    List<HttpRefResponseDto> getCustomHttpRefs(long userId, String sortBy);

    HttpRefResponseDto createCustomHttpRef(long userId, CreateHttpRequestDto createHttpRequestDto);
}
