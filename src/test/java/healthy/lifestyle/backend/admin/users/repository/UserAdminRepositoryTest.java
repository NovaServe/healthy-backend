package healthy.lifestyle.backend.admin.users.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Import(DataConfiguration.class)
public class UserAdminRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    UserAdminRepository userAdminRepository;

    @Autowired
    DataHelper dataHelper;

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("multipleValidFilters")
    void findByFiltersTest_shouldReturnListOfUsers_whenValidFilterGiven(
            String roleName,
            String username,
            String email,
            String fullName,
            String countryName,
            Integer age,
            List<Integer> resultSeeds) {

        // Given
        Role roleUser = dataHelper.createRole("ROLE_USER");
        Role roleAdmin = dataHelper.createRole("ROLE_ADMIN");

        Country country1 = dataHelper.createCountry(1);
        Country country2 = dataHelper.createCountry(2);

        User user1 = dataHelper.createUser("1", roleUser, country1, null, 34);
        User user2 = dataHelper.createUser("2", roleUser, country2, null, 16);
        User admin1 = dataHelper.createUser("3", roleAdmin, country2, null, 45);
        User admin2 = dataHelper.createUser("4", roleAdmin, country2, null, 21);

        Optional<Role> roleFilter = Stream.of(roleUser, roleAdmin)
                .filter(role -> role.getName().equals(roleName))
                .findFirst();
        Optional<Country> countryFilter = Stream.of(country1, country2)
                .filter(country -> country.getName().equals(countryName))
                .findFirst();

        // When
        Optional<List<User>> resultOptional = userAdminRepository.findByFilters(
                roleFilter.orElse(null), username, email, fullName, countryFilter.orElse(null), age);

        // Then
        assertTrue(resultOptional.isPresent());
        List<User> result = resultOptional.get();

        assertEquals(resultSeeds.size(), result.size());
        for (int i = 0; i < resultSeeds.size(); i++) {
            assertEquals("username-" + resultSeeds.get(i), result.get(i).getUsername());
            assertEquals(
                    "username-" + resultSeeds.get(i) + "@email.com",
                    result.get(i).getEmail());
            assertEquals("Full Name " + resultSeeds.get(i), result.get(i).getFullName());
        }
    }

    static Stream<Arguments> multipleValidFilters() {
        return Stream.of(
                Arguments.of(null, null, null, null, null, null, List.of(1, 2, 3, 4)),
                Arguments.of(
                        "ROLE_USER", "NameUser", "FullNameUser", "EmailUser", "Country 3", 78, Collections.emptyList()),
                Arguments.of("ROLE_USER", null, null, null, null, null, List.of(1, 2)),
                Arguments.of(
                        "ROLE_USER", "username-1", "username-1@email.com", "Full Name 1", "Country 1", 34, List.of(1)),
                Arguments.of("ROLE_USER", "username-1", null, null, null, null, List.of(1)),
                Arguments.of("ROLE_USER", null, "username-2@email.com", null, null, null, List.of(2)),
                Arguments.of("ROLE_USER", null, null, "Full Name 1", null, null, List.of(1)),
                Arguments.of("ROLE_USER", null, null, null, "Country 2", null, List.of(2)),
                Arguments.of("ROLE_USER", null, null, null, null, 16, List.of(2)),
                Arguments.of("ROLE_ADMIN", "username-4", null, null, null, null, List.of(4)),
                Arguments.of("ROLE_ADMIN", null, "username-3@email.com", null, null, null, List.of(3)),
                Arguments.of("ROLE_ADMIN", null, null, "Full Name 3", null, null, List.of(3)),
                Arguments.of("ROLE_ADMIN", null, null, null, "Country 2", null, List.of(3, 4)),
                Arguments.of("ROLE_ADMIN", null, null, null, null, 45, List.of(3)),
                Arguments.of(
                        "ROLE_ADMIN", "username-4", "username-4@email.com", "Full Name 4", "Country 2", 21, List.of(4)),
                Arguments.of("ROLE_ADMIN", null, null, null, null, null, List.of(3, 4)),
                Arguments.of(null, "username-2", null, null, null, null, List.of(2)),
                Arguments.of(null, null, "username-3@email.com", null, null, null, List.of(3)),
                Arguments.of(null, null, null, "Full Name 2", null, null, List.of(2)),
                Arguments.of(null, null, null, null, "Country 2", null, List.of(2, 3, 4)),
                Arguments.of(null, null, null, null, "Country 1", null, List.of(1)),
                Arguments.of(null, null, null, null, null, 21, List.of(4)));
    }
}
