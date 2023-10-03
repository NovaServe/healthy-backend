package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface HttpRefService {
    List<HttpRefResponseDto> getHttpRefs(long userId, Sort sort, boolean isCustomOnly);
}
