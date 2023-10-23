package healthy.lifestyle.backend.users.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.security.JwtTokenProvider;
import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.LoginResponseDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.dto.SignupResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CountryRepository countryRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.countryRepository = countryRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
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
        User user = new User.Builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .fullName(requestDto.getFullName())
                .role(role)
                .country(country)
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
}
