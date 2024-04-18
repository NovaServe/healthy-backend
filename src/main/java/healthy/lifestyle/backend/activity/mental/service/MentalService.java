package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import org.springframework.data.domain.Page;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId);

    Page<MentalResponseDto> getMentals(
            Long userId, String sortField, String sortDirection, int currentPageNumber, int pageSize);
}
