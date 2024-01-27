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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                    throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
                });

        HttpRef httpRefSaved = httpRefRepository.save(HttpRef.builder()
                .name(createHttpRequestDto.getName())
                .description(createHttpRequestDto.getDescription())
                .ref(createHttpRequestDto.getRef())
                .isCustom(true)
                .user(user)
                .build());

        HttpRefResponseDto responseDto = modelMapper.map(httpRefSaved, HttpRefResponseDto.class);
        return responseDto;
    }

    @Override
    public HttpRefResponseDto getCustomHttpRefById(long userId, long httpRefId) {
        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.NOT_FOUND));

        if (!httpRef.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

        HttpRefResponseDto responseDto = modelMapper.map(httpRef, HttpRefResponseDto.class);
        return responseDto;
    }

    @Override
    public Page<HttpRefResponseDto> getHttpRefs(
            Boolean isCustom,
            Long userId,
            String name,
            String description,
            String sortField,
            String sortDirection,
            int pageNumber,
            int pageSize) {
        Pageable pageable =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        Page<HttpRef> httpRefPage = null;
        if (isCustom != null)
            httpRefPage =
                    httpRefRepository.findDefaultOrCustomWithFilter(isCustom, userId, name, description, pageable);
        else httpRefPage = httpRefRepository.findDefaultAndCustomWithFilter(userId, name, description, pageable);

        Page<HttpRefResponseDto> httpRefResponseDtoPage =
                httpRefPage.map(httpRef -> modelMapper.map(httpRef, HttpRefResponseDto.class));
        return httpRefResponseDtoPage;
    }

    @Override
    public List<HttpRefResponseDto> getDefaultHttpRefs(Sort sort) {
        List<HttpRefResponseDto> responseDtoList = httpRefRepository.findAllDefault(sort).stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
        return responseDtoList;
    }

    @Override
    public List<HttpRefResponseDto> getCustomHttpRefs(long userId, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);
        List<HttpRefResponseDto> responseDtoList = httpRefRepository.findCustomByUserId(userId, sort).stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
        return responseDtoList;
    }

    @Override
    public HttpRefResponseDto updateCustomHttpRef(long userId, long httpRefId, HttpRefUpdateRequestDto requestDto) {
        if (requestDto.getName() == null && requestDto.getDescription() == null && requestDto.getRef() == null)
            throw new ApiException(ErrorMessage.EMPTY_REQUEST, null, HttpStatus.BAD_REQUEST);

        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.NOT_FOUND));

        if (!httpRef.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

        if (requestDto.getName() != null && !requestDto.getName().equals(httpRef.getName()))
            httpRef.setName(requestDto.getName());

        if (requestDto.getDescription() != null && !requestDto.getDescription().equals(httpRef.getDescription()))
            httpRef.setDescription(requestDto.getDescription());

        if (requestDto.getRef() != null && !requestDto.getRef().equals(httpRef.getRef()))
            httpRef.setRef(requestDto.getRef());

        HttpRefResponseDto responseDto = modelMapper.map(httpRefRepository.save(httpRef), HttpRefResponseDto.class);
        return responseDto;
    }

    @Override
    public void deleteCustomHttpRef(long userId, long httpRefId) {
        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.NOT_FOUND));

        if (!httpRef.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

        httpRefRepository.delete(httpRef);
    }
}
