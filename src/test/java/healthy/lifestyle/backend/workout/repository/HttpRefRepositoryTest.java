package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Import(DataConfiguration.class)
class HttpRefRepositoryTest {
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
    HttpRefRepository httpRefRepository;

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
    void findAllDefaultTest_shouldReturnAllDefaultHttpRefs() {
        // Given
        List<HttpRef> httpRefsDefault = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<HttpRef> httpRefsCustom = IntStream.rangeClosed(3, 4)
                .mapToObj(id -> dataHelper.createHttpRef(id, true))
                .toList();

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findAllDefault(sort);

        // Then
        assertEquals(httpRefsDefault.size(), httpRefsActual.size());

        assertThat(httpRefsDefault)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(httpRefsActual);
    }

    @Test
    void findCustomByUserIdTest_shouldReturnUserCustomHttpRefs_whenValidUserIdProvided() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        Country country1 = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country1, null, 20);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef1, user1);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        dataHelper.httpRefAddUser(httpRef2, user1);

        Country country2 = dataHelper.createCountry(2);
        User user2 = dataHelper.createUser("two", role, country2, null, 20);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        dataHelper.httpRefAddUser(httpRef3, user2);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, true);
        dataHelper.httpRefAddUser(httpRef4, user2);

        HttpRef defaultHttpRef1 = dataHelper.createHttpRef(5, false);
        HttpRef defaultHttpRef2 = dataHelper.createHttpRef(6, false);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findCustomByUserId(user1.getId(), sort);

        // Then
        assertEquals(2, httpRefsActual.size());

        assertThat(List.of(httpRef1, httpRef2))
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(httpRefsActual);
    }

    @Test
    void findCustomByUserIdTest_shouldReturnEmptyList_whenNoHttpRefsFound() {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        Country country1 = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country1, null, 20);

        Country country2 = dataHelper.createCountry(2);
        User user2 = dataHelper.createUser("two", role, country2, null, 20);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        dataHelper.httpRefAddUser(httpRef3, user2);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, true);
        dataHelper.httpRefAddUser(httpRef4, user2);

        HttpRef defaultHttpRef1 = dataHelper.createHttpRef(5, false);
        HttpRef defaultHttpRef2 = dataHelper.createHttpRef(6, false);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findCustomByUserId(user1.getId(), sort);

        // Then
        assertEquals(0, httpRefsActual.size());
    }
}
