package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.UpdateHttpRefRequestDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HttpRefServiceImpl implements HttpRefService {
    private final HttpRefRepository httpRefRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;

    public HttpRefServiceImpl(HttpRefRepository httpRefRepository, ModelMapper modelMapper, UserService userService) {
        this.httpRefRepository = httpRefRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    @Override
    public List<HttpRefResponseDto> getDefaultHttpRefs(Sort sort) {
        return httpRefRepository.findAllDefault(sort).stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
    }

    @Override
    public List<HttpRefResponseDto> getCustomHttpRefs(long userId, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);

        List<HttpRef> httpRefs = httpRefRepository.findCustomByUserId(userId, sort);

        return httpRefs.stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
    }

    @Transactional
    @Override
    public HttpRefResponseDto createCustomHttpRef(long userId, CreateHttpRequestDto createHttpRequestDto) {
        User user = userService.getUserById(userId);
        if (isNull(user)) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);

        Optional<HttpRef> httpRefAlreadyExistsOpt =
                httpRefRepository.findCustomByNameAndUserId(createHttpRequestDto.getName(), userId);
        if (httpRefAlreadyExistsOpt.isPresent())
            throw new ApiException(ErrorMessage.ALREADY_EXISTS, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = HttpRef.builder()
                .name(createHttpRequestDto.getName())
                .description(createHttpRequestDto.getDescription())
                .ref(createHttpRequestDto.getRef())
                .isCustom(true)
                .user(user)
                .build();

        HttpRef httpRefSaved = httpRefRepository.save(httpRef);

        return modelMapper.map(httpRefSaved, HttpRefResponseDto.class);
    }

    @Override
    public HttpRefResponseDto updateCustomHttpRef(long userId, long httpRefId, UpdateHttpRefRequestDto requestDto) {
        Optional<HttpRef> httpRefOptional = httpRefRepository.findById(httpRefId);
        if (httpRefOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = httpRefOptional.get();

        if (!httpRef.isCustom())
            throw new ApiException(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        if (nonNull(requestDto.getUpdatedName()) && !requestDto.getUpdatedName().equals(httpRef.getName())) {
            httpRef.setName(requestDto.getUpdatedName());
        }

        if (nonNull(requestDto.getUpdatedDescription())
                && !requestDto.getUpdatedDescription().equals(httpRef.getDescription())) {
            httpRef.setDescription(requestDto.getUpdatedDescription());
        }

        if (nonNull(requestDto.getUpdatedRef()) && !requestDto.getUpdatedRef().equals(httpRef.getRef())) {
            httpRef.setRef(requestDto.getUpdatedRef());
        }

        HttpRef updated = httpRefRepository.save(httpRef);
        return modelMapper.map(updated, HttpRefResponseDto.class);
    }
}
