package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalActivityCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.mental.repository.MentalActivityRepository;
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
public class MentalActivityServiceImpl implements MentalActivityService {
    @Autowired
    MentalActivityRepository mentalRepository;

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
    public MentalActivityResponseDto getMentalActivityById(long mentalId, boolean requiredDefault, Long userId) {
        MentalActivity mental = mentalRepository
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
                    && (user.getMentalActivities() == null
                            || !user.getMentalActivities().contains(mental)))
                throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalId, HttpStatus.BAD_REQUEST);
        }

        MentalActivityResponseDto mentalResponseDto = modelMapper.map(mental, MentalActivityResponseDto.class);

        List<HttpRefResponseDto> httpRefsSorted = mentalResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();
        mentalResponseDto.setHttpRefs(httpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public Page<MentalActivityResponseDto> getMentalActivities(
            Long userId, String sortField, String sortDirection, int currentPageNumber, int pageSize) {

        Pageable pageable = PageRequest.of(
                currentPageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        Page<MentalActivity> entityPage = mentalRepository.findDefaultAndCustomMentalActivity(userId, pageable);

        Page<MentalActivityResponseDto> dtoPage = entityPage.map(entity -> {
            MentalActivityResponseDto mentalResponseDto = modelMapper.map(entity, MentalActivityResponseDto.class);
            return mentalResponseDto;
        });

        return dtoPage;
    }

    @Override
    @Transactional
    public MentalActivityResponseDto updateCustomMentalActivity(
            long userId, long mentalId, MentalActivityUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {
        MentalActivity mental = mentalRepository
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
            List<MentalActivity> mentalWithSameTitle =
                    mentalRepository.getDefaultAndCustomMentalActivityByTitleAndUserId(requestDto.getTitle(), userId);
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

        MentalActivity savedMental = mentalRepository.save(mental);
        MentalActivityResponseDto responseDto = mapMentalToMentalResponseDto(savedMental);
        return responseDto;
    }

    private void updateHttpRefs(MentalActivityUpdateRequestDto requestDto, MentalActivity mental, Long userId) {
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

    private MentalActivityResponseDto mapMentalToMentalResponseDto(MentalActivity mental) {
        MentalActivityResponseDto mentalResponseDto = modelMapper.map(mental, MentalActivityResponseDto.class);
        List<HttpRefResponseDto> mentalHttpRefsSorted = mental.getHttpRefsSortedById().stream()
                .map(httpRef -> modelMapper.map(httpRef, HttpRefResponseDto.class))
                .toList();

        mentalResponseDto.setHttpRefs(mentalHttpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public void deleteCustomMentalActivity(long mentalId, long userId) {
        MentalActivity mental = mentalRepository
                .findCustomByMentalIdAndUserId(mentalId, userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalId, HttpStatus.NOT_FOUND));
        userService.deleteMentalActivitiesFromUser(userId, mental);
        mentalRepository.delete(mental);
    }

    @Override
    @Transactional
    public MentalActivityResponseDto createCustomMental(long userId, MentalActivityCreateRequestDto requestDto) {
        List<MentalActivity> mentalWithSameTitle =
                mentalRepository.getDefaultAndCustomMentalActivityByTitleAndUserId(requestDto.getTitle(), userId);

        if (!mentalWithSameTitle.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserById(userId);
        MentalActivity mental = MentalActivity.builder()
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

        MentalActivity mentalSaved = mentalRepository.save(mental);
        userService.addMentalActivitiesToUser(userId, mentalSaved);
        MentalActivityResponseDto mentalResponseDto = modelMapper.map(mentalSaved, MentalActivityResponseDto.class);
        List<HttpRefResponseDto> httpRefsSorted = mentalResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();

        mentalResponseDto.setHttpRefs(httpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public Page<MentalActivityResponseDto> getMentalActivitiesWithFilter(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Long mentalTypeId,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize) {

        Pageable pageable = PageRequest.of(
                currentPageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        Page<MentalActivity> entitiesPage = null;

        // Default and custom
        if (isCustom == null && userId != null) {
            entitiesPage =
                    mentalRepository.findDefaultAndCustomWithFilter(userId, title, description, mentalTypeId, pageable);
        }
        // Default only
        else if (isCustom != null && !isCustom && userId == null) {
            entitiesPage = mentalRepository.findDefaultOrCustomWithFilter(
                    false, null, title, description, mentalTypeId, pageable);
        }
        // Custom only
        else if (isCustom != null && isCustom && userId != null) {
            entitiesPage = mentalRepository.findDefaultOrCustomWithFilter(
                    true, userId, title, description, mentalTypeId, pageable);
        } else {
            throw new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
        }

        Page<MentalActivityResponseDto> dtoPage =
                entitiesPage.map(entity -> modelMapper.map(entity, MentalActivityResponseDto.class));
        return dtoPage;
    }
}
