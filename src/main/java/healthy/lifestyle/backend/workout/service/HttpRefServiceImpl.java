package healthy.lifestyle.backend.workout.service;

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
        User user = Optional.ofNullable(userService.getUserById(userId))
                .orElseThrow(() -> new ApiException(ErrorMessage.USER_NOT_FOUND, userId, HttpStatus.NOT_FOUND));

        httpRefRepository
                .findCustomByNameAndUserId(createHttpRequestDto.getName(), userId)
                .ifPresent(alreadyExistentWithSameTitle -> {
                    throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.BAD_REQUEST);
                });

        HttpRef httpRefSaved = httpRefRepository.save(HttpRef.builder()
                .name(createHttpRequestDto.getName())
                .description(createHttpRequestDto.getDescription())
                .ref(createHttpRequestDto.getRef())
                .isCustom(true)
                .user(user)
                .build());

        return modelMapper.map(httpRefSaved, HttpRefResponseDto.class);
    }

    @Override
    public HttpRefResponseDto getCustomHttpRefById(long userId, long httpRefId) {
        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.NOT_FOUND));

        if (!httpRef.isCustom()) throw new ApiException(ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

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
        return httpRefRepository.findCustomByUserId(userId, sort).stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
    }

    @Override
    public HttpRefResponseDto updateCustomHttpRef(long userId, long httpRefId, HttpRefUpdateRequestDto requestDto) {
        if (requestDto.getName() == null && requestDto.getDescription() == null && requestDto.getRef() == null)
            throw new ApiException(ErrorMessage.EMPTY_REQUEST, null, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.BAD_REQUEST));

        if (!httpRef.isCustom())
            throw new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

        if (requestDto.getName() != null && !requestDto.getName().equals(httpRef.getName()))
            httpRef.setName(requestDto.getName());

        if (requestDto.getDescription() != null && !requestDto.getDescription().equals(httpRef.getDescription()))
            httpRef.setDescription(requestDto.getDescription());

        if (requestDto.getRef() != null && !requestDto.getRef().equals(httpRef.getRef()))
            httpRef.setRef(requestDto.getRef());

        return modelMapper.map(httpRefRepository.save(httpRef), HttpRefResponseDto.class);
    }

    @Override
    public void deleteCustomHttpRef(long userId, long httpRefId) {
        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.BAD_REQUEST));

        if (!httpRef.isCustom())
            throw new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

        httpRefRepository.delete(httpRef);
    }
}
