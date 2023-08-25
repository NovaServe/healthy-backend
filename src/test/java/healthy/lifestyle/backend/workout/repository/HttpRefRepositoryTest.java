package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.Set;
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

/**
 * @see ExerciseRepository
 */
@SpringBootTest
@Testcontainers
@Import(DataConfiguration.class)
class HttpRefRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            (PostgreSQLContainer<?>) new PostgreSQLContainer(DockerImageName.parse("postgres:12.15"))
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withReuse(true);

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> String.format(
                        "jdbc:postgresql://localhost:%s/%s",
                        postgresqlContainer.getFirstMappedPort(), postgresqlContainer.getDatabaseName()));
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
    void findAllDefault() {
        HttpRef httpRef1 = dataHelper.createHttpRef("Ref 1", "https://ref1.com", "Desc 1", false);
        HttpRef httpRef2 = dataHelper.createHttpRef("Ref 2", "https://ref2.com", "Desc 2", false);
        dataHelper.createHttpRef("Ref 3", "https://ref3.com", "Desc 3", true);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<HttpRef> httpRefs = httpRefRepository.findAllDefault(sort);
        assertEquals(2, httpRefs.size());

        assertEquals(httpRef1.getId(), httpRefs.get(0).getId());
        assertEquals(httpRef1.getName(), httpRefs.get(0).getName());
        assertEquals(httpRef1.getRef(), httpRefs.get(0).getRef());
        assertEquals(httpRef1.getDescription(), httpRefs.get(0).getDescription());

        assertEquals(httpRef2.getId(), httpRefs.get(1).getId());
        assertEquals(httpRef2.getName(), httpRefs.get(1).getName());
        assertEquals(httpRef2.getRef(), httpRefs.get(1).getRef());
        assertEquals(httpRef2.getDescription(), httpRefs.get(1).getDescription());
    }

    @Test
    void findByUserId() {
        HttpRef httpRef1 = dataHelper.createHttpRef("Ref 1", "https://ref1.com", "Desc 1", false);
        // Http refs for test user
        HttpRef httpRef2 = dataHelper.createHttpRef("Ref 2", "https://ref2.com", "Desc 2", true);
        HttpRef httpRef3 = dataHelper.createHttpRef("Ref 3", "https://ref3.com", "Desc 3", true);
        // End
        HttpRef httpRef4 = dataHelper.createHttpRef("Ref 4", "https://ref4.com", "Desc 4", true);

        Exercise exercise1 = dataHelper.createExercise("Exercise 1", "Title 1", false, null, Set.of(httpRef1));
        Exercise exercise2 = dataHelper.createExercise("Exercise 2", "Title 2", false, null, Set.of(httpRef1));
        // Exercises of the test user
        Exercise exercise3 = dataHelper.createExercise("Exercise 3", "Title 3", true, null, Set.of(httpRef2, httpRef3));
        Exercise exercise4 = dataHelper.createExercise("Exercise 4", "Title 4", true, null, Set.of(httpRef1, httpRef3));
        // End
        Exercise exercise5 = dataHelper.createExercise("Exercise 5", "Title 5", true, null, Set.of(httpRef4));

        Role role = dataHelper.createRole("ROLE_USER");
        // Test user
        User user1 = dataHelper.createUser(
                "Full Name", "username-one", "user1@email.com", "password1", role, Set.of(exercise3, exercise4));
        User user2 = dataHelper.createUser(
                "Full Name", "username-two", "user2@email.com", "password2", role, Set.of(exercise5));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<HttpRef> httpRefs = httpRefRepository.findByUserId(user1.getId(), sort);
        assertEquals(2, httpRefs.size());

        assertEquals(httpRef2.getId(), httpRefs.get(0).getId());
        assertEquals(httpRef2.getName(), httpRefs.get(0).getName());
        assertEquals(httpRef2.getRef(), httpRefs.get(0).getRef());
        assertEquals(httpRef2.getDescription(), httpRefs.get(0).getDescription());

        assertEquals(httpRef3.getId(), httpRefs.get(1).getId());
        assertEquals(httpRef3.getName(), httpRefs.get(1).getName());
        assertEquals(httpRef3.getRef(), httpRefs.get(1).getRef());
        assertEquals(httpRef3.getDescription(), httpRefs.get(1).getDescription());
    }
}
