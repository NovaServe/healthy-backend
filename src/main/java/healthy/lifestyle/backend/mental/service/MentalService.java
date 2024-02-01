package healthy.lifestyle.backend.mental.service;

import healthy.lifestyle.backend.mental.dto.MentalResponseDto;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId);
}
