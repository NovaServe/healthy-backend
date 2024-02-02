package healthy.lifestyle.backend.mentals.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.mentals.dto.MentalResponseDto;
import healthy.lifestyle.backend.mentals.model.Mental;
import healthy.lifestyle.backend.mentals.model.MentalType;
import healthy.lifestyle.backend.mentals.repository.MentalRepository;
import healthy.lifestyle.backend.mentals.repository.MentalTypeRepository;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentalServiceImpl implements MentalService {

    private final MentalRepository mentalRepository;

    private final HttpRefRepository httpRefRepository;

    private final UserService userService;

    private final MentalTypeRepository mentalTypeRepository;

    private final ModelMapper modelMapper;

    public MentalServiceImpl(
            MentalRepository mentalRepository,
            HttpRefRepository httpRefRepository,
            UserService userService,
            MentalTypeRepository mentalTypeRepository,
            ModelMapper modelMapper) {
        this.mentalRepository = mentalRepository;
        this.httpRefRepository = httpRefRepository;
        this.userService = userService;
        this.mentalTypeRepository = mentalTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public MentalResponseDto getMentalById(long mentalId, boolean customRequire) {
        Mental mental = mentalRepository
                .findById(mentalId)
                .orElseThrow(() -> new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalId, HttpStatus.NOT_FOUND));

        if (mental.isCustom() && !customRequire)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!mental.isCustom() && customRequire)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        // if (userId != null) {
        //   User user = userService.getUserById(userId);
        //   if (mental.isCustom()
        //           && (user.getMentals() == null || !user.getMentals().contains(mental)))
        //      throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalId, HttpStatus.BAD_REQUEST);
        // }

        MentalResponseDto mentalResponseDto = modelMapper.map(mental, MentalResponseDto.class);

        List<HttpRefResponseDto> httpRefsSorted = mentalResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();
        mentalResponseDto.setHttpRefs(httpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public Page<MentalResponseDto> getMentalWithFilter(
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

        Page<Mental> entitiesPage = null;
        Optional<MentalType> mentalType =
                mentalTypeId != null ? mentalTypeRepository.findById(mentalTypeId) : Optional.empty();

        // Default and custom
        if (isCustom == null && userId != null) {
            entitiesPage = mentalRepository.findDefaultAndCustomWithFilter(
                    userId, title, description, mentalType.orElse(null), pageable);
        }
        // Default only
        else if (isCustom != null && !isCustom && userId == null) {
            entitiesPage = mentalRepository.findDefaultOrCustomWithFilter(
                    false, null, title, description, mentalType.orElse(null), pageable);
        }
        // Custom only
        else if (isCustom != null && isCustom && userId != null) {
            entitiesPage = mentalRepository.findDefaultOrCustomWithFilter(
                    true, userId, title, description, mentalType.orElse(null), pageable);
        } else {
            throw new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
        }

        Page<MentalResponseDto> dtoPage = entitiesPage.map(entity -> modelMapper.map(entity, MentalResponseDto.class));
        return dtoPage;
    }

    // @Override
    // @Transactional
    // public List<MentalResponseDto> getCustomMentals(long userId) {
    //      Sort sort = Sort.by(Sort.Direction.ASC, "id");
    //     List<MentalResponseDto> responseDtoList = mentalRepository.findCustomMentalByUserId(userId, sort).stream()
    //            .map(elt -> modelMapper.map(elt, MentalResponseDto.class))
    //              .peek(elt -> {
    //                  List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
    //                         .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
    //                        .toList();
    //              elt.setHttpRefs(httpRefsSorted);
    //           })
    //           .toList();
    //  return responseDtoList;
    // }

    // @Override
    // @Transactional
    //  public List<MentalResponseDto> getDefaultMentals() {
    //     Sort sort = Sort.by(Sort.Direction.ASC, "id");
    //     List<MentalResponseDto> responseDtoList = mentalRepository.findAllDefault(sort).stream()
    //            .map(mental -> modelMapper.map(mental, MentalResponseDto.class))
    //             .peek(elt -> {
    //                List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
    //                       .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
    //                       .toList();

    //              elt.setHttpRefs(httpRefsSorted);
    //          })
    //         .toList();
    //  return responseDtoList;
    // }

    @Override
    @Transactional
    public List<MentalResponseDto> getMentals(String sortBy, boolean isDefault, Long userId) {
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);
        List<Mental> mentals;
        if (isDefault) mentals = mentalRepository.findAllDefault(sort);
        else mentals = mentalRepository.findCustomMentalByUserId(userId, sort);

        List<MentalResponseDto> responseDtoList = mentals.stream()
                .map(mental -> modelMapper.map(mental, MentalResponseDto.class))
                .peek(elt -> {
                    List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
                            .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                            .toList();

                    elt.setHttpRefs(httpRefsSorted);
                })
                .toList();
        return responseDtoList;
    }
}
