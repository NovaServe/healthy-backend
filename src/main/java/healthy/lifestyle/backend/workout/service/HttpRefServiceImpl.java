package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class HttpRefServiceImpl implements HttpRefService {
    private final HttpRefRepository httpRefRepository;
    private final ModelMapper modelMapper;

    public HttpRefServiceImpl(HttpRefRepository httpRefRepository, ModelMapper modelMapper) {
        this.httpRefRepository = httpRefRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<HttpRefResponseDto> getHttpRefs(long userId, Sort sort, boolean isDefaultOnly) {
        List<HttpRef> httpRefs = new LinkedList<>();

        if (isDefaultOnly) {
            httpRefs = httpRefRepository.findAllDefault(sort);
        } else {
            httpRefs.addAll(httpRefRepository.findAllDefault(sort));
            httpRefs.addAll(httpRefRepository.findCustomByUserId(userId, sort));
            httpRefs.sort(Comparator.comparingLong(HttpRef::getId));
        }

        if (httpRefs.isEmpty()) throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        return httpRefs.stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
    }
}
