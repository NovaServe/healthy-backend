package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalUpdateRequestDto;
import org.springframework.data.domain.Page;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId);

    Page<MentalResponseDto> getMentals(
            Long userId, String sortField, String sortDirection, int currentPageNumber, int pageSize);

    MentalResponseDto updateCustomMental(long userId, long mentalId, MentalUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException;

    void deleteCustomMental(long mentalId, long userId);

    MentalResponseDto createCustomMental(long userId, MentalCreateRequestDto requestDto);
}
