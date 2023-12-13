package healthy.lifestyle.backend.admin.users.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    void findByFiltersTest_shouldReturnListOfUsers() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);

        // When
        List<User> result = userAdminRepository
                .findByFilters(
                        role, user1.getUsername(), user1.getEmail(), user1.getFullName(), country, user1.getAge())
                .get();

        // Then
        assertEquals(1, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user1.getUsername(), result.get(0).getUsername());
        assertEquals(user1.getEmail(), result.get(0).getEmail());
        assertEquals(user1.getFullName(), result.get(0).getFullName());
        assertEquals(user1.getCountry().getId(), result.get(0).getCountry().getId());
        assertEquals(user1.getAge(), result.get(0).getAge());
    }

    @Test
    void findByFiltersTest_shouldReturnListOfUsersWithRoleAdmin() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country, null, 18);

        Role role1 = dataHelper.createRole("ROLE_ADMIN");
        Country country1 = dataHelper.createCountry(2);
        User user2 = dataHelper.createUser("two", role1, country1, null, 25);

        Country country2 = dataHelper.createCountry(3);
        User user3 = dataHelper.createUser("second", role1, country2, null, 26);

        // When
        List<User> result = userAdminRepository
                .findByFilters(role1, null, null, null, null, null)
                .get();

        // Then
        assertEquals(2, result.size());
        assertEquals(user2.getId(), result.get(0).getId());
        assertEquals(user3.getId(), result.get(1).getId());
        assertEquals(user2.getRole().getName(), result.get(0).getRole().getName());
        assertEquals(user3.getRole().getName(), result.get(1).getRole().getName());
    }

    @Test
    void findByFiltersTest_shouldReturnListOfUsersWithSameCountry() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country, null, 18);

        Role role1 = dataHelper.createRole("ROLE_ADMIN");
        User user2 = dataHelper.createUser("two", role1, country, null, 25);

        Country country1 = dataHelper.createCountry(2);
        User user3 = dataHelper.createUser("second", role, country1, null, 26);
        User user4 = dataHelper.createUser("first", role1, country, null, 29);

        // When
        List<User> result = userAdminRepository
                .findByFilters(role1, null, null, null, country, null)
                .get();

        // Then
        assertEquals(2, result.size());
        assertEquals(user2.getId(), result.get(0).getId());
        assertEquals(user4.getId(), result.get(1).getId());
        assertEquals(user2.getCountry().getId(), result.get(0).getCountry().getId());
        assertEquals(user4.getCountry().getId(), result.get(1).getCountry().getId());
    }

    @Test
    void findByFiltersTest_shouldReturnEmptyListOfUsers() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country, null, 18);

        User user3 = dataHelper.createUser("second", role, country, null, 26);

        // When
        List<User> result = userAdminRepository
                .findByFilters(role, "oneTwo", null, null, null, null)
                .get();

        // Then
        assertEquals(0, result.size());
    }
}
