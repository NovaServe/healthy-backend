package healthy.lifestyle.backend.admin.user.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.Shared;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Import(BeanConfig.class)
@ActiveProfiles("test")
public class UserAdminRepositoryTest {
    @MockBean
    FirebaseMessaging firebaseMessaging;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    UserAdminRepository userAdminRepository;

    @Autowired
    DbUtil dbUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("findWithFilterValidFilters")
    void findWithFilter_shouldReturnListOfUsers_whenValidFilters(
            String roleName,
            String username,
            String email,
            String fullName,
            String countryName,
            Integer age,
            List<Integer> resultSeeds) {

        // Given
        Role roleUser = dbUtil.createUserRole();
        Role roleAdmin = dbUtil.createAdminRole();

        Country country1 = dbUtil.createCountry(1);
        Country country2 = dbUtil.createCountry(2);
        Country country3 = dbUtil.createCountry(3);
        Timezone timezone = dbUtil.createTimezone(1);

        User user1 = dbUtil.createUser(1, roleUser, country1, 34, timezone);
        User user2 = dbUtil.createUser(2, roleUser, country2, 16, timezone);
        User admin1 = dbUtil.createUser(3, roleAdmin, country2, 45, timezone);
        User admin2 = dbUtil.createUser(4, roleAdmin, country2, 21, timezone);

        Optional<Role> roleFilter = Stream.of(roleUser, roleAdmin)
                .filter(role -> role.getName().equals(roleName))
                .findFirst();
        Optional<Country> countryFilter = Stream.of(country1, country2, country3)
                .filter(country -> country.getName().equals(countryName))
                .findFirst();

        // When
        List<User> resultList = userAdminRepository.findWithFilter(
                roleFilter.orElse(null), username, email, fullName, countryFilter.orElse(null), age);

        // Then
        assertEquals(resultSeeds.size(), resultList.size());
        for (int i = 0; i < resultSeeds.size(); i++) {
            assertEquals("Username-" + resultSeeds.get(i), resultList.get(i).getUsername());
            assertEquals(
                    "email-" + resultSeeds.get(i) + "@email.com",
                    resultList.get(i).getEmail());
            assertEquals(
                    "Full Name " + Shared.numberToText(resultSeeds.get(i)),
                    resultList.get(i).getFullName());
        }
    }

    static Stream<Arguments> findWithFilterValidFilters() {
        return Stream.of(
                // Positive cases for ROLE_USER
                Arguments.of("ROLE_USER", null, null, null, null, null, List.of(1, 2)),
                Arguments.of(
                        "ROLE_USER", "Username-1", "email-1@email.com", "Full Name One", "Country 1", 34, List.of(1)),
                Arguments.of("ROLE_USER", "Username-1", null, null, null, null, List.of(1)),
                Arguments.of("ROLE_USER", null, "email-2@email.com", null, null, null, List.of(2)),
                Arguments.of("ROLE_USER", null, null, "Full Name One", null, null, List.of(1)),
                Arguments.of("ROLE_USER", null, null, null, "Country 2", null, List.of(2)),
                Arguments.of("ROLE_USER", null, null, null, null, 16, List.of(2)),

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
                Arguments.of("ROLE_USER", null, null, null, "Country 3", null, Collections.emptyList()),
                Arguments.of("ROLE_USER", null, null, null, null, 100, Collections.emptyList()),

                // Positive cases for ROLE_ADMIN
                Arguments.of("ROLE_ADMIN", null, null, null, null, null, List.of(3, 4)),
                Arguments.of(
                        "ROLE_ADMIN", "Username-4", "email-4@email.com", "Full Name Four", "Country 2", 21, List.of(4)),
                Arguments.of("ROLE_ADMIN", "Username-4", null, null, null, null, List.of(4)),
                Arguments.of("ROLE_ADMIN", null, "email-3@email.com", null, null, null, List.of(3)),
                Arguments.of("ROLE_ADMIN", null, null, "Full Name Three", null, null, List.of(3)),
                Arguments.of("ROLE_ADMIN", null, null, null, "Country 2", null, List.of(3, 4)),
                Arguments.of("ROLE_ADMIN", null, null, null, null, 45, List.of(3)),

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
                Arguments.of("ROLE_ADMIN", null, null, null, "Country 3", null, Collections.emptyList()),
                Arguments.of("ROLE_ADMIN", null, null, null, null, 100, Collections.emptyList()),

                // Positive cases for all roles
                Arguments.of(null, null, null, null, null, null, List.of(1, 2, 3, 4)),
                Arguments.of(null, "Username-2", null, null, null, null, List.of(2)),
                Arguments.of(null, null, "email-3@email.com", null, null, null, List.of(3)),
                Arguments.of(null, null, null, "Full Name Two", null, null, List.of(2)),
                Arguments.of(null, null, null, null, "Country 1", null, List.of(1)),
                Arguments.of(null, null, null, null, "Country 2", null, List.of(2, 3, 4)),
                Arguments.of(null, null, null, null, null, 21, List.of(4)),

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
                Arguments.of(null, null, null, null, "Country 3", null, Collections.emptyList()),
                Arguments.of(null, null, null, null, null, 100, Collections.emptyList()));
    }
}
