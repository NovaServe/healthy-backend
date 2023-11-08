package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.UserRepository;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.Comparator;
import java.util.LinkedList;
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
    private final UserRepository userRepository;

    public HttpRefServiceImpl(
            HttpRefRepository httpRefRepository,
            ModelMapper modelMapper,
            UserService userService,
            UserRepository userRepository) {
        this.httpRefRepository = httpRefRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public List<HttpRefResponseDto> getDefaultHttpRefs(Sort sort) {
        return httpRefRepository.findAllDefault(sort).stream()
                .map(elt -> modelMapper.map(elt, HttpRefResponseDto.class))
                .toList();
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
}
