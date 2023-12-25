package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
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

    @Transactional
    @Override
    public HttpRefResponseDto createCustomHttpRef(long userId, HttpRefCreateRequestDto createHttpRequestDto) {
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
    public HttpRefResponseDto getCustomHttpRefById(long userId, long httpRefId) {
        Optional<HttpRef> httpRefOptional = httpRefRepository.findById(httpRefId);
        if (httpRefOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = httpRefOptional.get();

        if (!httpRef.isCustom()) throw new ApiException(ErrorMessage.DEFAULT_MEDIA_REQUESTED, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        return modelMapper.map(httpRef, HttpRefResponseDto.class);
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

    @Override
    public HttpRefResponseDto updateCustomHttpRef(long userId, long httpRefId, HttpRefUpdateRequestDto requestDto) {
        if (isNull(requestDto.getName()) && isNull(requestDto.getDescription()) && isNull(requestDto.getRef()))
            throw new ApiException(ErrorMessage.EMPTY_REQUEST, HttpStatus.BAD_REQUEST);

        Optional<HttpRef> httpRefOptional = httpRefRepository.findById(httpRefId);
        if (httpRefOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = httpRefOptional.get();

        if (!httpRef.isCustom())
            throw new ApiException(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        if (nonNull(requestDto.getName()) && !requestDto.getName().equals(httpRef.getName())) {
            httpRef.setName(requestDto.getName());
        }

        if (nonNull(requestDto.getDescription()) && !requestDto.getDescription().equals(httpRef.getDescription())) {
            httpRef.setDescription(requestDto.getDescription());
        }

        if (nonNull(requestDto.getRef()) && !requestDto.getRef().equals(httpRef.getRef())) {
            httpRef.setRef(requestDto.getRef());
        }

        HttpRef updated = httpRefRepository.save(httpRef);
        return modelMapper.map(updated, HttpRefResponseDto.class);
    }

    @Override
    public long deleteCustomHttpRef(long userId, long httpRefId) {
        Optional<HttpRef> httpRefOptional = httpRefRepository.findById(httpRefId);
        if (httpRefOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = httpRefOptional.get();

        if (!httpRef.isCustom())
            throw new ApiException(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        httpRefRepository.delete(httpRef);

        return httpRefId;
    }
}
