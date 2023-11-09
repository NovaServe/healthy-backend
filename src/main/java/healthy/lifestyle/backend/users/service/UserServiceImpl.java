package healthy.lifestyle.backend.users.service;

import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
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

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CountryRepository countryRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.countryRepository = countryRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.modelMapper = modelMapper;
    }

    @Override
    public SignupResponseDto createUser(SignupRequestDto requestDto) {
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

        User saved = userRepository.save(user);
        return new SignupResponseDto.Builder().id(saved.getId()).build();
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

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void addExercise(long userId, Exercise exercise) {
        User user = userRepository.getReferenceById(userId);
        user.getExercises().add(exercise);
        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public User getUserById(long userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto requestDto) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        User user = userOptional.get();

        if (nonNull(requestDto.getUpdatedCountryId())) {
            Optional<Country> countryOptional = countryRepository.findById(requestDto.getUpdatedCountryId());
            if (countryOptional.isEmpty())
                throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            user.setCountry(countryOptional.get());
        }

        if (nonNull(requestDto.getUpdatedUsername())
                && !requestDto.getUpdatedUsername().isEmpty()
                && !requestDto.getUpdatedUsername().equals(user.getUsername())) {
            user.setUsername(requestDto.getUpdatedUsername());
        }

        if (nonNull(requestDto.getUpdatedEmail())
                && !requestDto.getUpdatedEmail().isEmpty()
                && !requestDto.getUpdatedEmail().equals(user.getEmail())) {
            user.setEmail(requestDto.getUpdatedEmail());
        }

        if (nonNull(requestDto.getUpdatedPassword())
                && !requestDto.getUpdatedPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestDto.getUpdatedPassword()));
        }

        if (nonNull(requestDto.getUpdatedFullName())
                && !requestDto.getUpdatedFullName().isEmpty()
                && !requestDto.getUpdatedFullName().equals(user.getFullName())) {
            user.setFullName(requestDto.getUpdatedFullName());
        }

        if (nonNull(requestDto.getUpdatedAge()) && !requestDto.getUpdatedAge().equals(user.getAge())) {
            user.setAge(requestDto.getUpdatedAge());
        }

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }
}
