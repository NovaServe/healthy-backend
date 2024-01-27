package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface HttpRefService {
    HttpRefResponseDto createCustomHttpRef(long userId, HttpRefCreateRequestDto createHttpRequestDto);

    HttpRefResponseDto getCustomHttpRefById(long userId, long httpRefId);

    Page<HttpRefResponseDto> getHttpRefs(
            Boolean isCustom,
            Long userId,
            String name,
            String description,
            String sortField,
            String sortDirection,
            int pageNumber,
            int pageSize);

    List<HttpRefResponseDto> getDefaultHttpRefs(Sort sort);

    List<HttpRefResponseDto> getCustomHttpRefs(long userId, String sortBy);

    HttpRefResponseDto updateCustomHttpRef(
            long userId, long httpRefId, HttpRefUpdateRequestDto updateHttpRefRequestDto);

    void deleteCustomHttpRef(long userId, long httpRefId);
}
