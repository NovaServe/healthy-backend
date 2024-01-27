package healthy.lifestyle.backend.workout.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Import(BeanConfig.class)
class HttpRefRepositoryTest {
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
    HttpRefRepository httpRefRepository;

    @Autowired
    DbUtil dbUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    void findAllDefaultTest_shouldReturnDefaultHttpRefList() {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findAllDefault(sort);

        // Then
        assertEquals(2, httpRefsActual.size());

        assertThat(httpRefsActual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "mentals", "nutritions")
                .isEqualTo(List.of(defaultHttpRef1, defaultHttpRef2));
    }

    @ParameterizedTest
    @MethodSource("multipleValidFiltersDefaultOrCustom")
    void findDefaultOrCustomWithFilterTest_shouldReturnDefaultOrCustomHttpRefs(
            boolean isCustom,
            Long userId,
            String name,
            String description,
            int totalElements,
            int totalPages,
            int numberOfElements) {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);
        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(6, user2);

        if (userId != null) userId = user1.getId();
        int currentPageNumber = 0;
        int itemsPerPage = 2;
        String orderBy = "ASC";
        String sortBy = "id";
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(orderBy), sortBy));

        // When
        Page<HttpRef> httpRefPage =
                httpRefRepository.findDefaultOrCustomWithFilter(isCustom, userId, name, description, pageable);

        // Then
        assertEquals(totalElements, httpRefPage.getTotalElements());
        assertEquals(totalPages, httpRefPage.getTotalPages());
        assertEquals(numberOfElements, httpRefPage.getNumberOfElements());
        assertEquals(numberOfElements, httpRefPage.getContent().size());
        assertEquals(currentPageNumber, httpRefPage.getNumber());
    }

    static Stream<Arguments> multipleValidFiltersDefaultOrCustom() {
        return Stream.of(
                // Default
                Arguments.of(false, null, "1", null, 1, 1, 1),
                Arguments.of(false, null, null, "1", 1, 1, 1),
                Arguments.of(false, null, "1", "1", 1, 1, 1),
                Arguments.of(false, null, "1", "2", 0, 0, 0),
                Arguments.of(false, null, "Name", null, 3, 2, 2),
                Arguments.of(false, null, null, "Desc", 3, 2, 2),
                Arguments.of(false, null, null, null, 3, 2, 2),

                // Custom
                Arguments.of(true, 0L, "4", null, 1, 1, 1),
                Arguments.of(true, 0L, null, "4", 1, 1, 1),
                Arguments.of(true, 0L, "4", "4", 1, 1, 1),
                Arguments.of(true, 0L, "1", "2", 0, 0, 0),
                Arguments.of(true, 0L, "Name", null, 2, 1, 2),
                Arguments.of(true, 0L, null, "Desc", 2, 1, 2),
                Arguments.of(true, 0L, null, null, 2, 1, 2));
    }

    @ParameterizedTest
    @MethodSource("multipleFiltersDefaultOrCustomSortAndOrder")
    void findDefaultOrCustomWithFilterTest_shouldReturnDefaultOrCustomHttpRefs_whenSortAndOrder(
            String sortBy, String orderBy, Boolean isCustom) {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(6, user);

        int currentPageNumber = 0;
        int itemsPerPage = 3;
        Long userId = null;
        String name = null;
        String description = null;
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(orderBy), sortBy));
        int totalElements = 3;

        // When
        Page<HttpRef> httpRefPage =
                httpRefRepository.findDefaultOrCustomWithFilter(isCustom, userId, name, description, pageable);

        // Then
        assertEquals(totalElements, httpRefPage.getTotalElements());
        assertEquals(currentPageNumber, httpRefPage.getNumber());

        if (orderBy.equals("ASC") && !isCustom) {
            assertThat(httpRefPage.getContent().get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefPage.getContent().get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefPage.getContent().get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef3);
        }

        if (orderBy.equals("DESC") && !isCustom) {
            assertThat(httpRefPage.getContent().get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef3);

            assertThat(httpRefPage.getContent().get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefPage.getContent().get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef1);
        }

        if (orderBy.equals("ASC") && isCustom) {
            assertThat(httpRefPage.getContent().get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefPage.getContent().get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefPage.getContent().get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef3);
        }

        if (orderBy.equals("DESC") && isCustom) {
            assertThat(httpRefPage.getContent().get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef3);

            assertThat(httpRefPage.getContent().get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefPage.getContent().get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef1);
        }
    }

    static Stream<Arguments> multipleFiltersDefaultOrCustomSortAndOrder() {
        return Stream.of(
                // Default
                Arguments.of("id", "ASC", false),
                Arguments.of("name", "ASC", false),
                Arguments.of("description", "ASC", false),
                Arguments.of("id", "DESC", false),
                Arguments.of("name", "DESC", false),
                Arguments.of("description", "DESC", false),

                // Custom
                Arguments.of("id", "ASC", true),
                Arguments.of("name", "ASC", true),
                Arguments.of("description", "ASC", true),
                Arguments.of("id", "DESC", true),
                Arguments.of("name", "DESC", true),
                Arguments.of("description", "DESC", true));
    }

    @ParameterizedTest
    @MethodSource("multipleValidFiltersDefaultAndCustom")
    void findDefaultAndCustomWithFilterTest_shouldReturnDefaultAndCustomHttpRefs(
            String name, String description, int totalElements, int totalPages, int numberOfElements) {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);
        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(6, user2);

        long userId = user1.getId();
        int currentPageNumber = 0;
        int itemsPerPage = 2;
        String orderBy = "ASC";
        String sortBy = "id";
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(orderBy), sortBy));

        // When
        Page<HttpRef> httpRefPage =
                httpRefRepository.findDefaultAndCustomWithFilter(userId, name, description, pageable);

        // Then
        assertEquals(totalElements, httpRefPage.getTotalElements());
        assertEquals(totalPages, httpRefPage.getTotalPages());
        assertEquals(numberOfElements, httpRefPage.getNumberOfElements());
        assertEquals(numberOfElements, httpRefPage.getContent().size());
        assertEquals(currentPageNumber, httpRefPage.getNumber());
    }

    static Stream<Arguments> multipleValidFiltersDefaultAndCustom() {
        return Stream.of(
                // Default
                Arguments.of("1", null, 1, 1, 1),
                Arguments.of(null, "1", 1, 1, 1),
                Arguments.of("1", "1", 1, 1, 1),

                // Custom
                Arguments.of("4", null, 1, 1, 1),
                Arguments.of(null, "4", 1, 1, 1),
                Arguments.of("4", "4", 1, 1, 1),

                // Default and custom
                Arguments.of("1", "2", 0, 0, 0),
                Arguments.of("Name", null, 5, 3, 2),
                Arguments.of(null, "Desc", 5, 3, 2),
                Arguments.of(null, null, 5, 3, 2));
    }

    @ParameterizedTest
    @MethodSource("multipleFiltersDefaultAndCustomSortAndOrder")
    void findDefaultOrCustomWithFilterTest_shouldReturnDefaultAndCustomHttpRefs_whenSortAndOrder(
            String sortBy, String orderBy) {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user1);
        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef4 = dbUtil.createCustomHttpRef(5, user2);

        int currentPageNumber = 0;
        int itemsPerPage = 4;
        Long userId = user1.getId();
        String name = null;
        String description = null;
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(orderBy), sortBy));
        int totalElements = 4;
        int numberOfElements = 4;

        // When
        Page<HttpRef> httpRefPage =
                httpRefRepository.findDefaultAndCustomWithFilter(userId, name, description, pageable);

        // Then
        assertEquals(totalElements, httpRefPage.getTotalElements());
        assertEquals(currentPageNumber, httpRefPage.getNumber());
        assertEquals(numberOfElements, httpRefPage.getNumberOfElements());
        assertEquals(itemsPerPage, httpRefPage.getContent().size());

        if (orderBy.equals("ASC")) {
            assertThat(httpRefPage.getContent().get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefPage.getContent().get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefPage.getContent().get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefPage.getContent().get(3))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef2);
        }

        if (orderBy.equals("DESC")) {
            assertThat(httpRefPage.getContent().get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefPage.getContent().get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefPage.getContent().get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefPage.getContent().get(3))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "nutritions", "mentals")
                    .isEqualTo(defaultHttpRef1);
        }
    }

    static Stream<Arguments> multipleFiltersDefaultAndCustomSortAndOrder() {
        return Stream.of(
                Arguments.of("id", "ASC"),
                Arguments.of("name", "ASC"),
                Arguments.of("description", "ASC"),
                Arguments.of("id", "DESC"),
                Arguments.of("name", "DESC"),
                Arguments.of("description", "DESC"));
    }

    @Test
    void findCustomByUserIdTest_shouldReturnUserCustomHttpRefList() {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user1);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(5, user2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findCustomByUserId(user1.getId(), sort);

        // Then
        assertEquals(2, httpRefsActual.size());

        assertThat(httpRefsActual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals", "nutritions")
                .isEqualTo(List.of(customHttpRef1, customHttpRef2));
    }

    @Test
    void findCustomByUserIdTest_shouldReturnEmptyList_whenNoHttpRefsFound() {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(3, user2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<HttpRef> httpRefsActual = httpRefRepository.findCustomByUserId(user1.getId(), sort);

        // Then
        assertEquals(0, httpRefsActual.size());
    }
}
