package healthy.lifestyle.backend.mental.service;

import healthy.lifestyle.backend.mental.dto.MentalResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean customRequire);

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

    // List<MentalResponseDto> getDefaultMentals();

    // List<MentalResponseDto> getCustomMentals(long userId);

    List<MentalResponseDto> getMentals(String sortBy, boolean isDefault, Long userId);
}
