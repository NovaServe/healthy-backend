package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.mental.repository.MentalRepository;
import healthy.lifestyle.backend.activity.mental.repository.MentalTypeRepository;
import healthy.lifestyle.backend.activity.workout.dto.*;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import java.util.*;
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
public class MentalServiceImpl implements MentalService {
    @Autowired
    MentalRepository mentalRepository;

    @Autowired
    HttpRefRepository httpRefRepository;

    @Autowired
    UserService userService;

    @Autowired
    MentalTypeRepository mentalTypeRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    VerificationUtil verificationUtil;

    @Override
    @Transactional
    public MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId) {
        Mental mental = mentalRepository
                .findById(mentalId)
                .orElseThrow(() -> new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalId, HttpStatus.NOT_FOUND));

        if (mental.isCustom() && requiredDefault)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!mental.isCustom() && !requiredDefault)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (userId != null) {
            User user = userService.getUserById(userId);
            if (mental.isCustom()
                    && (user.getMentals() == null || !user.getMentals().contains(mental)))
                throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalId, HttpStatus.BAD_REQUEST);
        }

        MentalResponseDto mentalResponseDto = modelMapper.map(mental, MentalResponseDto.class);

        List<HttpRefResponseDto> httpRefsSorted = mentalResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();
        mentalResponseDto.setHttpRefs(httpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public Page<MentalResponseDto> getMentals(
            Long userId, String sortField, String sortDirection, int currentPageNumber, int pageSize) {

        Pageable pageable = PageRequest.of(
                currentPageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        Page<Mental> entityPage = mentalRepository.findDefaultAndCustomMentals(userId, pageable);

        Page<MentalResponseDto> dtoPage = entityPage.map(entity -> {
            MentalResponseDto mentalResponseDto = modelMapper.map(entity, MentalResponseDto.class);
            return mentalResponseDto;
        });

        return dtoPage;
    }

    @Override
    @Transactional
    public MentalResponseDto updateCustomMental(long userId, long mentalId, MentalUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {
        Mental mental = mentalRepository
                .findById(mentalId)
                .orElseThrow(() -> new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalId, HttpStatus.NOT_FOUND));

        if (!mental.isCustom()) {
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);
        }

        if (userId != mental.getUser().getId()) {
            throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalId, HttpStatus.BAD_REQUEST);
        }

        boolean fieldsAreNull = verificationUtil.areFieldsNull(requestDto, "title", "description");
        boolean httpRefsAreDifferent =
                verificationUtil.areNestedEntitiesDifferent(mental.getHttpRefsIdsSorted(), requestDto.getHttpRefIds());
        boolean mentalTypeAreDifferent = verificationUtil.areNestedEntitiesDifferent(
                Collections.singletonList(mental.getType().getId()),
                Collections.singletonList(requestDto.getMentalTypeId()));

        if (fieldsAreNull && !httpRefsAreDifferent && !mentalTypeAreDifferent) {
            throw new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);
        }

        List<String> fieldsWithSameValues =
                verificationUtil.getFieldsWithSameValues(mental, requestDto, "title", "description");
        if (!fieldsWithSameValues.isEmpty()) {
            String errorMessage =
                    ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + String.join(", ", fieldsWithSameValues);
            throw new ApiExceptionCustomMessage(errorMessage, HttpStatus.BAD_REQUEST);
        }

        if (requestDto.getTitle() != null) {
            List<Mental> mentalWithSameTitle =
                    mentalRepository.getDefaultAndCustomMentalByTitleAndUserId(requestDto.getTitle(), userId);
            if (!mentalWithSameTitle.isEmpty()) {
                throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
            }
            mental.setTitle(requestDto.getTitle());
        }

        if (requestDto.getDescription() != null) {
            mental.setDescription(requestDto.getDescription());
        }
        if (requestDto.getMentalTypeId() != null) {
            MentalType mentalType = mentalTypeRepository
                    .findById(requestDto.getMentalTypeId())
                    .orElseThrow(() -> new ApiException(
                            ErrorMessage.MENTAL_TYPE_NOT_FOUND, requestDto.getMentalTypeId(), HttpStatus.NOT_FOUND));
            mental.setType(mentalType);
        }
        if (httpRefsAreDifferent) updateHttpRefs(requestDto, mental, userId);

        Mental savedMental = mentalRepository.save(mental);
        MentalResponseDto responseDto = mapMentalToMentalResponseDto(savedMental);
        return responseDto;
    }

    private void updateHttpRefs(MentalUpdateRequestDto requestDto, Mental mental, Long userId) {
        User user = userService.getUserById(userId);

        if (requestDto.getHttpRefIds().isEmpty()) {
            mental.getHttpRefs().clear();
            return;
        }

        Set<Long> idsToAdd = new HashSet<>(requestDto.getHttpRefIds());
        Set<Long> idsToRemove = new HashSet<>();

        for (HttpRef httpRef : mental.getHttpRefs()) {
            idsToAdd.remove(httpRef.getId());
            if (!requestDto.getHttpRefIds().contains(httpRef.getId())) idsToRemove.add(httpRef.getId());
        }

        for (long id : idsToAdd) {
            HttpRef httpRef = httpRefRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, id, HttpStatus.NOT_FOUND));

            if (httpRef.isCustom()) {
                boolean userHasCustomHttpRef =
                        user.getHttpRefs() != null && user.getHttpRefs().contains(httpRef);
                if (!userHasCustomHttpRef)
                    throw new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, id, HttpStatus.BAD_REQUEST);
            }

            mental.getHttpRefs().add(httpRef);
        }

        for (long id : idsToRemove) {
            HttpRef httpRef = httpRefRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, id, HttpStatus.NOT_FOUND));
            mental.getHttpRefs().remove(httpRef);
        }
    }

    private MentalResponseDto mapMentalToMentalResponseDto(Mental mental) {
        MentalResponseDto mentalResponseDto = modelMapper.map(mental, MentalResponseDto.class);
        List<HttpRefResponseDto> mentalHttpRefsSorted = mental.getHttpRefsSortedById().stream()
                .map(httpRef -> modelMapper.map(httpRef, HttpRefResponseDto.class))
                .toList();

        mentalResponseDto.setHttpRefs(mentalHttpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public MentalResponseDto createCustomMental(long userId, MentalCreateRequestDto requestDto) {
        List<Mental> mentalWithSameTitle =
                mentalRepository.getDefaultAndCustomMentalByTitleAndUserId(requestDto.getTitle(), userId);

        if (!mentalWithSameTitle.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserById(userId);
        Mental mental = Mental.builder()
                .isCustom(true)
                .user(user)
                .title(requestDto.getTitle())
                .type(new MentalType())
                .httpRefs(new HashSet<>())
                .build();

        if (requestDto.getDescription() != null) mental.setDescription(requestDto.getDescription());
        if (requestDto.getHttpRefs() != null && requestDto.getHttpRefs().size() > 0)
            requestDto.getHttpRefs().forEach(id -> {
                HttpRef httpRef = httpRefRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, id, HttpStatus.NOT_FOUND));

                if (httpRef.isCustom() && httpRef.getUser().getId() != userId)
                    throw new ApiException(
                            ErrorMessage.USER_HTTP_REF_MISMATCH, httpRef.getId(), HttpStatus.BAD_REQUEST);

                mental.getHttpRefs().add(httpRef);
            });

        MentalType mentalType = mentalTypeRepository
                .findById(requestDto.getMentalTypeId())
                .orElseThrow(() -> new ApiException(
                        ErrorMessage.MENTAL_TYPE_NOT_FOUND, requestDto.getMentalTypeId(), HttpStatus.NOT_FOUND));
        mental.setType(mentalType);

        Mental mentalSaved = mentalRepository.save(mental);
        userService.addMentalToUser(userId, mentalSaved);
        MentalResponseDto mentalResponseDto = modelMapper.map(mentalSaved, MentalResponseDto.class);
        List<HttpRefResponseDto> httpRefsSorted = mentalResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();

        mentalResponseDto.setHttpRefs(httpRefsSorted);
        return mentalResponseDto;
    }
}
