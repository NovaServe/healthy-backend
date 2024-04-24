package healthy.lifestyle.backend.activity.workout.service;

import healthy.lifestyle.backend.activity.workout.dto.BodyPartResponseDto;
import java.util.List;

public interface BodyPartService {
    List<BodyPartResponseDto> getBodyParts();
}
