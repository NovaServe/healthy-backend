package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalActivityCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityUpdateRequestDto;
import org.springframework.data.domain.Page;

public interface MentalActivityService {
    MentalActivityResponseDto getMentalActivityById(long mentalId, boolean requiredDefault, Long userId);

    Page<MentalActivityResponseDto> getMentalActivities(
            Long userId, String sortField, String sortDirection, int currentPageNumber, int pageSize);

    MentalActivityResponseDto updateCustomMentalActivity(
            long userId, long mentalId, MentalActivityUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException;

    void deleteCustomMentalActivity(long mentalId, long userId);

    MentalActivityResponseDto createCustomMental(long userId, MentalActivityCreateRequestDto requestDto);

    Page<MentalActivityResponseDto> getMentalActivitiesWithFilter(
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
