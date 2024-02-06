package healthy.lifestyle.backend.mental.service;

import healthy.lifestyle.backend.mental.dto.MentalResponseDto;
import org.springframework.data.domain.Page;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId);

    Page<MentalResponseDto> getMentalWithFilter(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Long mentalTypeId,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize);
}
