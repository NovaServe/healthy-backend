package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HttpRefServiceImpl implements HttpRefService {
    @Autowired
    HttpRefRepository httpRefRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserService userService;

    @Autowired
    VerificationUtil verificationUtil;

    @Override
    @Transactional
    public HttpRefResponseDto createCustomHttpRef(long userId, HttpRefCreateRequestDto requestDto) {
        User user = Optional.ofNullable(userService.getUserById(userId))
                .orElseThrow(() -> new ApiException(ErrorMessage.USER_NOT_FOUND, userId, HttpStatus.NOT_FOUND));

        List<HttpRef> httpRefsWithSameName =
                httpRefRepository.findDefaultAndCustomByNameAndUserId(requestDto.getName(), userId);
        if (!httpRefsWithSameName.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

        HttpRef httpRefSaved = httpRefRepository.save(HttpRef.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .ref(requestDto.getRef())
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
    public Page<HttpRefResponseDto> getHttpRefsWithFilter(
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
    public HttpRefResponseDto updateCustomHttpRef(long userId, long httpRefId, HttpRefUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {

        HttpRef httpRef = httpRefRepository
                .findById(httpRefId)
                .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, httpRefId, HttpStatus.NOT_FOUND));

        if (!httpRef.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        if (httpRef.getUser().getId() != userId)
            throw new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, httpRefId, HttpStatus.BAD_REQUEST);

        boolean fieldsAreNull = verificationUtil.areFieldsNull(requestDto, "name", "description", "ref");
        if (fieldsAreNull) {
            throw new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);
        }

        List<String> fieldsWithSameValues =
                verificationUtil.getFieldsWithSameValues(httpRef, requestDto, "name", "description", "ref");
        if (!fieldsWithSameValues.isEmpty()) {
            String errorMessage =
                    ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + String.join(", ", fieldsWithSameValues);
            throw new ApiExceptionCustomMessage(errorMessage, HttpStatus.BAD_REQUEST);
        }

        if (requestDto.getName() != null) {
            List<HttpRef> httpRefsWithSameName =
                    httpRefRepository.findDefaultAndCustomByNameAndUserId(requestDto.getName(), userId);
            if (!httpRefsWithSameName.isEmpty()) {
                throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
            }
            httpRef.setName(requestDto.getName());
        }

        if (requestDto.getDescription() != null) {
            httpRef.setDescription(requestDto.getDescription());
        }

        if (requestDto.getRef() != null) {
            httpRef.setRef(requestDto.getRef());
        }

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
