package healthy.lifestyle.backend.activity.workout.service;

import healthy.lifestyle.backend.activity.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefUpdateRequestDto;
import org.springframework.data.domain.Page;

public interface HttpRefService {
    HttpRefResponseDto createCustomHttpRef(long userId, HttpRefCreateRequestDto createHttpRequestDto);

    HttpRefResponseDto getCustomHttpRefById(long userId, long httpRefId);

    Page<HttpRefResponseDto> getHttpRefsWithFilter(
            Boolean isCustom,
            Long userId,
            String name,
            String description,
            String sortField,
            String sortDirection,
            int pageNumber,
            int pageSize);

    HttpRefResponseDto updateCustomHttpRef(long userId, long httpRefId, HttpRefUpdateRequestDto updateHttpRefRequestDto)
            throws NoSuchFieldException, IllegalAccessException;

    void deleteCustomHttpRef(long userId, long httpRefId);
}
