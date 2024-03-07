package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId);
}
