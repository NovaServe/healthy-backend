package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import java.util.List;

public interface BodyPartService {
    List<BodyPartResponseDto> getBodyParts();
}
