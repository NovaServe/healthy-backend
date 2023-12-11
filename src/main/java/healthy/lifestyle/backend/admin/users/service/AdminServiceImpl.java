package healthy.lifestyle.backend.admin.users.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    public AdminServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<UserResponseDto> getUsersByFilters(
            String username, String email, String fullName, Country country, Integer age) {
        List<User> users = userRepository.findByFilters(username, email, fullName, country, age);
        if (isNull(users) || users.size() == 0)
            throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        return users.stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .sorted(Comparator.comparing(UserResponseDto::getId))
                .toList();
    }
}
