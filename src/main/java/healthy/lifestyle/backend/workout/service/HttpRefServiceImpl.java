package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class HttpRefServiceImpl implements HttpRefService {
    private final HttpRefRepository httpRefRepository;

    public HttpRefServiceImpl(HttpRefRepository httpRefRepository) {
        this.httpRefRepository = httpRefRepository;
    }

    @Override
    public List<HttpRefResponseDto> getHttpRefs(long userId, Sort sort) {
        List<HttpRef> defaultHttpRefs = httpRefRepository.findAllDefault(sort);

        if (nonNull(defaultHttpRefs) && defaultHttpRefs.size() > 0) {
            List<HttpRef> response = new ArrayList<>(defaultHttpRefs);
            List<HttpRef> customHttpRefs = httpRefRepository.findByUserId(userId, sort);
            if (nonNull(customHttpRefs) && customHttpRefs.size() > 0) response.addAll(customHttpRefs);
            return mapHttpRefToHttpRefResponseDto(response);
        }

        throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private List<HttpRefResponseDto> mapHttpRefToHttpRefResponseDto(List<HttpRef> httpRefs) {
        if (isNull(httpRefs) || httpRefs.size() == 0)
            throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        List<HttpRefResponseDto> responseDtoList = new ArrayList<>();
        for (HttpRef httpRef : httpRefs) {
            HttpRefResponseDto dto = new HttpRefResponseDto.Builder()
                    .id(httpRef.getId())
                    .name(httpRef.getName())
                    .ref(httpRef.getRef())
                    .description(httpRef.getDescription())
                    .isCustom(httpRef.isCustom())
                    .build();
            responseDtoList.add(dto);
        }

        responseDtoList.sort(Comparator.comparingLong(HttpRefResponseDto::getId));
        return responseDtoList;
    }
}
