package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    void findCustomByUserIdTest_shouldReturnUserCustomHttpRefs_whenUserIdIsProvided() {
        // Given
        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefsDefault = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<HttpRef> httpRefsCustom = IntStream.rangeClosed(5, 8)
                .mapToObj(id -> dataHelper.createHttpRef(id, true))
                .toList();

        Exercise exercise1_ofUser1 = dataHelper.createExercise(
                1, true, false, new HashSet<>(bodyParts), Set.of(httpRefsDefault.get(0), httpRefsCustom.get(0)));

        Exercise exercise2_ofUser1 = dataHelper.createExercise(
                2, true, false, new HashSet<>(bodyParts), Set.of(httpRefsDefault.get(1), httpRefsCustom.get(1)));

        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user1_test = dataHelper.createUser("one", role, country, Set.of(exercise1_ofUser1, exercise2_ofUser1));

        Exercise exercise1_ofUser2 = dataHelper.createExercise(
                3, true, false, new HashSet<>(bodyParts), Set.of(httpRefsDefault.get(2), httpRefsCustom.get(2)));

        Exercise exercise2_ofUser2 = dataHelper.createExercise(
                4, true, false, new HashSet<>(bodyParts), Set.of(httpRefsDefault.get(2), httpRefsCustom.get(2)));

        User user2 = dataHelper.createUser("two", role, country, Set.of(exercise1_ofUser2, exercise2_ofUser2));

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findCustomByUserId(user1_test.getId(), sort);

        // Then
        assertEquals(2, httpRefsActual.size());

        assertEquals(httpRefsCustom.get(0).getId(), httpRefsActual.get(0).getId());
        assertEquals(httpRefsCustom.get(0).getName(), httpRefsActual.get(0).getName());
        assertEquals(httpRefsCustom.get(0).getRef(), httpRefsActual.get(0).getRef());
        assertEquals(
                httpRefsCustom.get(0).getDescription(), httpRefsActual.get(0).getDescription());

        assertEquals(httpRefsCustom.get(1).getId(), httpRefsActual.get(1).getId());
        assertEquals(httpRefsCustom.get(1).getName(), httpRefsActual.get(1).getName());
        assertEquals(httpRefsCustom.get(1).getRef(), httpRefsActual.get(1).getRef());
        assertEquals(
                httpRefsCustom.get(1).getDescription(), httpRefsActual.get(1).getDescription());
    }
}
