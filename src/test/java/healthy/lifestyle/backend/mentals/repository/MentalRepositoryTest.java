package healthy.lifestyle.backend.mentals.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.mentals.model.Mental;
import healthy.lifestyle.backend.mentals.model.MentalType;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.workout.model.HttpRef;
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
public class MentalRepositoryTest {
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
    MentalRepository mentalRepository;

    @Autowired
    DbUtil dbUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    static Stream<Arguments> findDefaultOrCustomWithFilter_multipleDefaultFilters() {
        return Stream.of(
                // Default, positive
                Arguments.of(null, null, "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of("mental", null, "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of(null, "desc", "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of("mental", "desc", "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of(null, null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of("mental", null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of(null, "desc", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of("mental", "desc", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of(null, null, null, 4, 0, 4, 1, 4, List.of(1L, 2L, 3L, 4L)),
                Arguments.of("mental", null, null, 4, 0, 4, 1, 4, List.of(1L, 2L, 3L, 4L)),
                Arguments.of(null, "desc", null, 4, 0, 4, 1, 4, List.of(1L, 2L, 3L, 4L)),
                Arguments.of("mental", "desc", null, 4, 0, 4, 1, 4, List.of(1L, 2L, 3L, 4L)),

                // Default, empty
                Arguments.of("non-existent", null, null, 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non-existent", null, 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @ParameterizedTest
    @MethodSource("findDefaultOrCustomWithFilter_multipleDefaultFilters")
    void findDefaultOrCustomWithFilterTest_shouldReturnDefaultFilteredMentals(
            String title,
            String description,
            String typeName,
            int itemsPerPage,
            int currentPageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds) {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        Mental customMental1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user);
        Mental customMental2 = dbUtil.createCustomMental(6, List.of(defaultHttpRef1), mentalType2, user);

        List<Mental> expectedFilteredMentals = Stream.of(
                        defaultMental1, defaultMental2, defaultMental3, defaultMental4, customMental1, customMental2)
                .filter(mental ->
                        resultSeeds.stream().anyMatch(seed -> mental.getTitle().contains(String.valueOf(seed))))
                .toList();

        String direction = "ASC";
        String sortBy = "id";
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction), sortBy));

        Optional<MentalType> mentalTypeFilter = Stream.of(mentalType1, mentalType2)
                .filter(type -> type.getName().equals(typeName))
                .findFirst();

        // When
        Page<Mental> mentalPage = mentalRepository.findDefaultOrCustomWithFilter(
                false, null, title, description, mentalTypeFilter.orElse(null), pageable);

        // Then
        assertEquals(totalElements, mentalPage.getTotalElements());
        assertEquals(totalPages, mentalPage.getTotalPages());
        assertEquals(resultSeeds.size(), mentalPage.getContent().size());
        assertEquals(numberOfElementsCurrentPage, mentalPage.getContent().size());
        assertEquals(currentPageNumber, mentalPage.getNumber());
        assertEquals(itemsPerPage, mentalPage.getSize());

        assertThat(mentalPage.getContent())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "user", "type")
                .isEqualTo(expectedFilteredMentals);
    }

    static Stream<Arguments> findDefaultAndCustomWithFilter_multipleFilters() {
        return Stream.of(
                // Positive
                Arguments.of(null, null, "MEDITATION", 3, 0, 3, 1, 3, List.of(2L, 4L, 6L)),
                // Arguments.of(null, null, "AFFIRMATION", 5, 0, 5, 1, 5, List.of(1L, 3L, 5L, 7L, 8L)),
                // Arguments.of(null, null, null, 8, 0, 8, 1, 8, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)),
                Arguments.of("mental", null, "MEDITATION", 3, 0, 3, 1, 3, List.of(2L, 4L, 6L)),
                // Arguments.of("mental", null, "AFFIRMATION", 5, 0, 5, 1, 5, List.of(1L, 3L, 5L, 7L, 8L)),
                // Arguments.of("mental", null, null, 8, 0, 8, 1, 8, List.of( 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)),

                // Empty
                Arguments.of("non-existent", null, "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non-existent", "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @ParameterizedTest
    @MethodSource("findDefaultAndCustomWithFilter_multipleFilters")
    void findDefaultAndCustomWithFilterTest_shouldReturnFilteredMentals(
            String title,
            String description,
            String typeName,
            int itemsPerPage,
            int currentPageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds) {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef2), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental1User1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user1);
        Mental customMental2User1 = dbUtil.createCustomMental(6, List.of(defaultHttpRef2), mentalType2, user1);
        Mental customMental3User1 = dbUtil.createCustomMental(7, List.of(defaultHttpRef3), mentalType1, user1);
        Mental customMental4User1 =
                dbUtil.createCustomMental(8, List.of(defaultHttpRef1, defaultHttpRef3), mentalType1, user1);

        Mental customMental1User2 =
                dbUtil.createCustomMental(9, List.of(defaultHttpRef1, defaultHttpRef2), mentalType2, user2);
        Mental customMental2User2 =
                dbUtil.createCustomMental(10, List.of(defaultHttpRef2, defaultHttpRef3), mentalType2, user2);
        List<Mental> expectedFilteredMentals = Stream.of(
                        defaultMental1,
                        defaultMental2,
                        defaultMental3,
                        defaultMental4,
                        customMental1User1,
                        customMental2User1,
                        customMental3User1,
                        customMental4User1,
                        customMental1User2,
                        customMental2User2)
                .filter(mental ->
                        resultSeeds.stream().anyMatch(seed -> mental.getTitle().contains(String.valueOf(seed))))
                .toList();

        String direction = "ASC";
        String sortBy = "id";
        Pageable pageable =
                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction), sortBy));

        Optional<MentalType> mentalTypeFilter = Stream.of(mentalType1, mentalType2)
                .filter(mentalType -> mentalType.getName().equals(typeName))
                .findFirst();

        Page<Mental> mentalPage = mentalRepository.findDefaultAndCustomWithFilter(
                user1.getId(), title, description, mentalTypeFilter.orElse(null), pageable);

        // Then
        assertEquals(totalElements, mentalPage.getTotalElements());
        assertEquals(totalPages, mentalPage.getTotalPages());
        assertEquals(resultSeeds.size(), mentalPage.getContent().size());
        assertEquals(numberOfElementsCurrentPage, mentalPage.getContent().size());
        assertEquals(currentPageNumber, mentalPage.getNumber());
        assertEquals(itemsPerPage, mentalPage.getSize());

        assertThat(mentalPage.getContent())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "user", "type")
                .isEqualTo(expectedFilteredMentals);

        assertThat(mentalPage.getContent())
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "type")
                .isEqualTo(expectedFilteredMentals);
    }

    @Test
    void findAllDefault_shouldReturnDefaultMentalList() {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        Mental customMental1 =
                dbUtil.createCustomMental(3, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);
        Mental customMental2 =
                dbUtil.createCustomMental(4, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<Mental> mentalsActual = mentalRepository.findAllDefault(sort);

        // Then
        assertEquals(2, mentalsActual.size());
        assertThat(mentalsActual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "users", "type")
                .isEqualTo(List.of(defaultMental1, defaultMental2));
    }

    @Test
    void findCustomByUserIdTest_shouldReturnCustomMentalList_whenUserIdGiven() {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);

        User user = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        Mental customMental1 =
                dbUtil.createCustomMental(2, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);

        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
        Mental customMental2 =
                dbUtil.createCustomMental(3, List.of(defaultHttpRef1, customHttpRef2), mentalType1, user);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(3, user2);
        MentalType mentalType2 = dbUtil.createMeditationType();
        Mental customMental3 =
                dbUtil.createCustomMental(4, List.of(defaultHttpRef1, customHttpRef3), mentalType2, user2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        List<Mental> mentalActual = mentalRepository.findCustomMentalByUserId(user.getId(), sort);

        // Then
        assertEquals(2, mentalActual.size());
        assertThat(mentalActual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("httpRefs", "user", "type")
                .isEqualTo(List.of(customMental1, customMental2));
    }
}
