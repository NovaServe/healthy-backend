package healthy.lifestyle.backend.admin.users.service;

import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.admin.users.repository.UserAdminRepository;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.util.Shared;
import healthy.lifestyle.backend.util.TestUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @InjectMocks
    UserAdminServiceImpl userAdminService;

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private RoleRepository roleRepository;

    @Spy
    private TestUtil testUtil;

    @Spy
    ModelMapper modelMapper;

    @ParameterizedTest
    @MethodSource("multipleFilters")
    void getUsersByFiltersTest_shouldReturnEntityResponseDtoList_whenIdAndParamsAreValid(
            String roleName,
            String username,
            String email,
            String fullName,
            String countryName,
            Integer age,
            List<Integer> resultSeeds) {
        Role roleUser = testUtil.createUserRole(1);
        Role roleAdmin = testUtil.createAdminRole(2);
        Country country1 = testUtil.createCountry(1);
        Country country2 = testUtil.createCountry(2);

        User user1 = testUtil.createUser(1, roleUser, country1);
        User user2 = testUtil.createUser(2, roleUser, country2);
        User user3 = testUtil.createUser(3, roleAdmin, country1);
        User user4 = testUtil.createUser(4, roleAdmin, country2);

        List<User> users = Arrays.asList(user1, user2, user3, user4);

        users = users.stream()
                .filter(user ->
                        resultSeeds.contains(Integer.parseInt(user.getUsername().split("-")[1])))
                .collect(Collectors.toList());

        // Mocking repository method
        when(countryRepository.findById(anyLong())).thenReturn(Optional.of(new Country()));
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(new Role()));
        when(userAdminRepository.findByFilters(
                        any(Role.class), eq(username), eq(email), eq(fullName), any(Country.class), eq(age)))
                .thenReturn(Optional.of(users));

        // When
        List<UserResponseDto> result = userAdminService.getUsersByFilters(
                roleUser.getName().equals(roleName) ? roleUser.getId() : roleAdmin.getId(),
                username,
                email,
                fullName,
                country1.getName().equals(countryName) ? country1.getId() : country2.getId(),
                age);

        // Then
        Assertions.assertEquals(users.size(), result.size());
        for (int i = 0; i < resultSeeds.size(); i++) {
            Assertions.assertEquals(
                    "Username-" + resultSeeds.get(i), result.get(i).getUsername());
            Assertions.assertEquals(
                    "email-" + resultSeeds.get(i) + "@email.com", result.get(i).getEmail());
            Assertions.assertEquals(
                    "Full Name " + Shared.numberToText(resultSeeds.get(i)),
                    result.get(i).getFullName());
        }
        verify(userAdminRepository)
                .findByFilters(any(Role.class), eq(username), eq(email), eq(fullName), any(Country.class), eq(age));
        verify(modelMapper, times(users.size())).map(any(), eq(UserResponseDto.class));
        verify(modelMapper, times(resultSeeds.size())).map(any(User.class), eq(UserResponseDto.class));
    }

    static Stream<Arguments> multipleFilters() {
        return Stream.of(
                // Positive cases for ROLE_USER
                Arguments.of("ROLE_USER", null, null, null, null, null, List.of(1, 2)),
                Arguments.of(
                        "ROLE_USER", "Username-1", "email-1@email.com", "Full Name One", "Country 1", 20, List.of(1)),
                Arguments.of("ROLE_USER", "Username-1", null, null, null, null, List.of(1)),
                Arguments.of("ROLE_USER", null, "email-2@email.com", null, null, null, List.of(2)),
                Arguments.of("ROLE_USER", null, null, "Full Name One", null, null, List.of(1)),
                Arguments.of("ROLE_USER", null, null, null, "Country 2", null, List.of(2)),
                Arguments.of("ROLE_USER", null, null, null, null, 20, List.of(1, 2)),

                // Negative cases for ROLE_USER
                Arguments.of(
                        "ROLE_USER",
                        "NonExistentValue",
                        "NonExistentValue",
                        "NonExistentValue",
                        "NonExistentValue",
                        100,
                        Collections.emptyList()),
                Arguments.of("ROLE_USER", "NonExistentValue", null, null, null, null, Collections.emptyList()),
                Arguments.of("ROLE_USER", null, "NonExistentValue", null, null, null, Collections.emptyList()),
                Arguments.of("ROLE_USER", null, null, "NonExistentValue", null, null, Collections.emptyList()),
                Arguments.of("ROLE_USER", null, null, null, "Country 100", null, Collections.emptyList()),
                Arguments.of("ROLE_USER", null, null, null, null, 100, Collections.emptyList()),

                // Positive cases for ROLE_ADMIN
                Arguments.of("ROLE_ADMIN", null, null, null, null, null, List.of(3)),
                Arguments.of(
                        "ROLE_ADMIN", "Username-4", "email-4@email.com", "Full Name Four", "Country 2", 20, List.of(4)),
                Arguments.of("ROLE_ADMIN", "Username-3", null, null, null, null, List.of(3)),
                Arguments.of("ROLE_ADMIN", null, "email-3@email.com", null, null, null, List.of(3)),
                Arguments.of("ROLE_ADMIN", null, null, "Full Name Four", null, null, List.of(4)),
                Arguments.of("ROLE_ADMIN", null, null, null, "Country 2", null, List.of(4)),
                Arguments.of("ROLE_ADMIN", null, null, null, null, 20, List.of(3, 4)),

                // Negative cases for ROLE_ADMIN
                Arguments.of(
                        "ROLE_ADMIN",
                        "NonExistentValue",
                        "NonExistentValue",
                        "NonExistentValue",
                        "NonExistentValue",
                        100,
                        Collections.emptyList()),
                Arguments.of("ROLE_ADMIN", "NonExistentValue", null, null, null, null, Collections.emptyList()),
                Arguments.of("ROLE_ADMIN", null, "NonExistentValue", null, null, null, Collections.emptyList()),
                Arguments.of("ROLE_ADMIN", null, null, "NonExistentValue", null, null, Collections.emptyList()),
                Arguments.of("ROLE_ADMIN", null, null, null, "Country 100", null, Collections.emptyList()),
                Arguments.of("ROLE_ADMIN", null, null, null, null, 100, Collections.emptyList()),

                // Positive cases for all roles
                Arguments.of(null, null, null, null, null, null, List.of(1, 2, 3, 4)),
                Arguments.of(null, "Username-2", null, null, null, null, List.of(2)),
                Arguments.of(null, null, "email-3@email.com", null, null, null, List.of(3)),
                Arguments.of(null, null, null, "Full Name Two", null, null, List.of(2)),
                Arguments.of(null, null, null, null, "Country 1", null, List.of(1, 3)),
                Arguments.of(null, null, null, null, "Country 2", null, List.of(2, 4)),
                Arguments.of(null, null, null, null, null, 20, List.of(1, 2, 3, 4)),

                // Negative cases for all roles
                Arguments.of(
                        null,
                        "NonExistentValue",
                        "NonExistentValue",
                        "NonExistentValue",
                        "NonExistentValue",
                        100,
                        Collections.emptyList()),
                Arguments.of(null, "NonExistentValue", null, null, null, null, Collections.emptyList()),
                Arguments.of(null, null, "NonExistentValue", null, null, null, Collections.emptyList()),
                Arguments.of(null, null, null, "NonExistentValue", null, null, Collections.emptyList()),
                Arguments.of(null, null, null, null, "Country 100", null, Collections.emptyList()),
                Arguments.of(null, null, null, null, null, 100, Collections.emptyList()));
    }
}
