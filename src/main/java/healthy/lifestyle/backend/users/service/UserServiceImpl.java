package healthy.lifestyle.backend.users.service;

import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.security.JwtTokenProvider;
import healthy.lifestyle.backend.users.dto.*;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.service.RemovalService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;
    private final RemovalService removalService;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CountryRepository countryRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            ModelMapper modelMapper,
            RemovalService removalService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.countryRepository = countryRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.modelMapper = modelMapper;
        this.removalService = removalService;
    }

    @Override
    public void createUser(SignupRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())
                || userRepository.existsByUsername(requestDto.getUsername())) {
            throw new ApiException(ErrorMessage.ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        Optional<Role> roleOpt = roleRepository.findByName("ROLE_USER");

        Country country;
        try {
            country = countryRepository.getReferenceById(requestDto.getCountryId());
        } catch (EntityNotFoundException e) {
            throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (roleOpt.isEmpty()) {
            throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Role role = roleOpt.get();
        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .fullName(requestDto.getFullName())
                .role(role)
                .country(country)
                .age(requestDto.getAge())
                .build();
        userRepository.save(user);
    }

    @Override
    public UserResponseDto getUserDetailsById(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND);

        return modelMapper.map(userOptional.get(), UserResponseDto.class);
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        updateUserCheckIfFieldsAreDifferent(requestDto, user);

        if (nonNull(requestDto.getUsername())) user.setUsername(requestDto.getUsername());
        if (nonNull(requestDto.getEmail())) user.setEmail(requestDto.getEmail());
        if (nonNull(requestDto.getPassword())) user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        if (nonNull(requestDto.getFullName())) user.setFullName(requestDto.getFullName());
        if (nonNull(requestDto.getAge())) user.setAge(requestDto.getAge());
        if (nonNull(requestDto.getCountryId())
                && requestDto.getCountryId() != user.getCountry().getId()) {
            Country country = countryRepository
                    .findById(requestDto.getCountryId())
                    .orElseThrow(
                            () -> new ApiException(ErrorMessage.COUNTRY_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR));
            user.setCountry(country);
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    private void updateUserCheckIfFieldsAreDifferent(UserUpdateRequestDto requestDto, User user) {
        StringBuilder errorMessage = new StringBuilder();

        if (nonNull(requestDto.getUsername()) && user.getUsername().equals(requestDto.getUsername()))
            errorMessage.append(ErrorMessage.USERNAME_IS_NOT_DIFFERENT.getName());

        if (nonNull(requestDto.getEmail()) && user.getEmail().equals(requestDto.getEmail())) {
            if (!errorMessage.isEmpty()) errorMessage.append(" ");
            errorMessage.append(ErrorMessage.EMAIL_IS_NOT_DIFFERENT.getName());
        }

        if (nonNull(requestDto.getFullName()) && user.getFullName().equals(requestDto.getFullName())) {
            if (!errorMessage.isEmpty()) errorMessage.append(" ");
            errorMessage.append(ErrorMessage.FULLNAME_IS_NOT_DIFFERENT.getName());
        }

        if (nonNull(requestDto.getAge()) && user.getAge() == requestDto.getAge()) {
            if (!errorMessage.isEmpty()) errorMessage.append(" ");
            errorMessage.append(ErrorMessage.AGE_IS_NOT_DIFFERENT.getName());
        }

        if (nonNull(requestDto.getPassword())
                && passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            if (!errorMessage.isEmpty()) errorMessage.append(" ");
            errorMessage.append(ErrorMessage.PASSWORD_IS_NOT_DIFFERENT.getName());
        }

        if (!errorMessage.isEmpty())
            throw new ApiExceptionCustomMessage(errorMessage.toString(), HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        removalService.deleteCustomWorkouts(user.getWorkoutsIdsSorted());
        removalService.deleteCustomExercises(user.getExercisesIdsSorted());
        removalService.deleteCustomHttpRefs(user.getCustomHttpRefsIdsSorted());
        userRepository.delete(user);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getUsernameOrEmail(), requestDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);
            return new LoginResponseDto(token);
        } catch (Exception e) {
            throw new ApiException(ErrorMessage.AUTHENTICATION_ERROR, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public User getUserById(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void addExercise(long userId, Exercise exercise) {
        User user = userRepository.getReferenceById(userId);
        user.getExercises().add(exercise);
        userRepository.save(user);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteUserExercise(long userId, Exercise exercise) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND));
        user.getExercises().remove(exercise);
        userRepository.save(user);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void addWorkout(User user, Workout workout) {
        user.getWorkouts().add(workout);
        userRepository.save(user);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeWorkout(User user, Workout workout) {
        user.getWorkouts().remove(workout);
    }
}
