package healthy.lifestyle.backend.mentals.service;

import healthy.lifestyle.backend.mentals.dto.MentalResponseDto;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean required, Long userId);
}
