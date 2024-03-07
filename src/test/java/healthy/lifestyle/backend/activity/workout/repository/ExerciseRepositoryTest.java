// package healthy.lifestyle.backend.workout.repository;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;
//
// import com.google.firebase.messaging.FirebaseMessaging;
// import healthy.lifestyle.backend.activity.workout.model.BodyPart;
// import healthy.lifestyle.backend.activity.workout.model.Exercise;
// import healthy.lifestyle.backend.activity.workout.model.HttpRef;
// import healthy.lifestyle.backend.activity.workout.repository.BodyPartRepository;
// import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
// import healthy.lifestyle.backend.config.BeanConfig;
// import healthy.lifestyle.backend.config.ContainerConfig;
// import healthy.lifestyle.backend.user.model.Country;
// import healthy.lifestyle.backend.user.model.Role;
// import healthy.lifestyle.backend.user.model.User;
// import healthy.lifestyle.backend.util.DbUtil;
// import java.util.*;
// import java.util.stream.Stream;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;
// import org.testcontainers.utility.DockerImageName;
//
// @SpringBootTest
// @Testcontainers
// @Import(BeanConfig.class)
// class ExerciseRepositoryTest {
//    @MockBean
//    FirebaseMessaging firebaseMessaging;
//
//    @Container
//    static PostgreSQLContainer<?> postgresqlContainer =
//            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));
//
//    @DynamicPropertySource
//    static void postgresProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
//        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
//    }
//
//    @Autowired
//    ExerciseRepository exerciseRepository;
//
//    @Autowired
//    BodyPartRepository bodyPartRepository;
//
//    @Autowired
//    DbUtil dbUtil;
//
//    @BeforeEach
//    void beforeEach() {
//        dbUtil.deleteAll();
//    }
//
//    @ParameterizedTest
//    @MethodSource("findDefaultOrCustomWithFilter_multipleDefaultFilters")
//    void findDefaultOrCustomWithFilterTest_shouldReturnDefaultFilteredExercises(
//            String title,
//            String description,
//            Boolean needsEquipment,
//            int itemsPerPage,
//            int currentPageNumber,
//            int totalElements,
//            int totalPages,
//            int numberOfElementsCurrentPage,
//            List<Long> resultSeeds) {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
//        List<Long> bodyPartsIds =
//                bodyPartRepository.findAll().stream().map(BodyPart::getId).toList();
//
//        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
//
//        Exercise defaultExercise1 =
//                dbUtil.createDefaultExercise(0, true, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef1));
//        Exercise defaultExercise2 =
//                dbUtil.createDefaultExercise(1, true, List.of(bodyPart3, bodyPart4), List.of(defaultHttpRef1));
//        Exercise defaultExercise3 =
//                dbUtil.createDefaultExercise(2, false, List.of(bodyPart3), List.of(defaultHttpRef1));
//        Exercise defaultExercise4 =
//                dbUtil.createDefaultExercise(3, false, List.of(bodyPart4), List.of(defaultHttpRef1));
//
//        User user = dbUtil.createUser(1);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(4, true, List.of(bodyPart1), List.of(defaultHttpRef1), user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(5, true, List.of(bodyPart2), List.of(defaultHttpRef1), user);
//
//        List<Exercise> expectedFilteredExercises = Stream.of(
//                        defaultExercise1,
//                        defaultExercise2,
//                        defaultExercise3,
//                        defaultExercise4,
//                        customExercise1,
//                        customExercise2)
//                .filter(exercise -> resultSeeds.stream()
//                        .anyMatch(seed -> exercise.getTitle().contains(String.valueOf(seed))))
//                .toList();
//
//        String direction = "ASC";
//        String sortBy = "id";
//        Pageable pageable =
//                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction),
// sortBy));
//
//        // When
//        Page<Exercise> exercisePage = exerciseRepository.findDefaultOrCustomWithFilter(
//                false, null, title, description, needsEquipment, bodyPartsIds, pageable);
//
//        // Then
//        assertEquals(totalElements, exercisePage.getTotalElements());
//        assertEquals(totalPages, exercisePage.getTotalPages());
//        assertEquals(resultSeeds.size(), exercisePage.getContent().size());
//        assertEquals(numberOfElementsCurrentPage, exercisePage.getContent().size());
//        assertEquals(currentPageNumber, exercisePage.getNumber());
//        assertEquals(itemsPerPage, exercisePage.getSize());
//
//        assertThat(exercisePage.getContent())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(expectedFilteredExercises);
//    }
//
//    static Stream<Arguments> findDefaultOrCustomWithFilter_multipleDefaultFilters() {
//        return Stream.of(
//                // Default, positive
//                Arguments.of(null, null, true, 2, 0, 2, 1, 2, List.of(0L, 1L)),
//                Arguments.of("exercise", null, true, 2, 0, 2, 1, 2, List.of(0L, 1L)),
//                Arguments.of(null, "desc", true, 2, 0, 2, 1, 2, List.of(0L, 1L)),
//                Arguments.of("exercise", "desc", true, 2, 0, 2, 1, 2, List.of(0L, 1L)),
//                Arguments.of(null, null, false, 2, 0, 2, 1, 2, List.of(2L, 3L)),
//                Arguments.of("exercise", null, false, 2, 0, 2, 1, 2, List.of(2L, 3L)),
//                Arguments.of(null, "desc", false, 2, 0, 2, 1, 2, List.of(2L, 3L)),
//                Arguments.of("exercise", "desc", false, 2, 0, 2, 1, 2, List.of(2L, 3L)),
//                Arguments.of(null, null, null, 4, 0, 4, 1, 4, List.of(0L, 1L, 2L, 3L)),
//                Arguments.of("exercise", null, null, 4, 0, 4, 1, 4, List.of(0L, 1L, 2L, 3L)),
//                Arguments.of(null, "desc", null, 4, 0, 4, 1, 4, List.of(0L, 1L, 2L, 3L)),
//                Arguments.of("exercise", "desc", null, 4, 0, 4, 1, 4, List.of(0L, 1L, 2L, 3L)),
//
//                // Default, empty
//                Arguments.of("non-existent", null, true, 2, 0, 0, 0, 0, Collections.emptyList()),
//                Arguments.of(null, "non-existent", false, 2, 0, 0, 0, 0, Collections.emptyList()));
//    }
//
//    @Test
//    void findDefaultOrCustomWithFilterTest_shouldReturnDefaultFilteredExercises_whenBodyPartsIdsGiven() {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
//        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
//
//        Exercise defaultExercise1 =
//                dbUtil.createDefaultExercise(0, true, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef1));
//        Exercise defaultExercise2 =
//                dbUtil.createDefaultExercise(1, true, List.of(bodyPart3, bodyPart4), List.of(defaultHttpRef1));
//        Exercise defaultExercise3 =
//                dbUtil.createDefaultExercise(2, false, List.of(bodyPart3), List.of(defaultHttpRef1));
//        Exercise defaultExercise4 =
//                dbUtil.createDefaultExercise(3, false, List.of(bodyPart4), List.of(defaultHttpRef1));
//
//        User user = dbUtil.createUser(1);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(4, true, List.of(bodyPart1), List.of(defaultHttpRef1), user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(5, true, List.of(bodyPart2), List.of(defaultHttpRef1), user);
//
//        String direction = "ASC";
//        String sortBy = "id";
//        int currentPageNumber = 0;
//        int itemsPerPage = 2;
//        Pageable pageable =
//                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction),
// sortBy));
//
//        // When
//        Page<Exercise> exercisePage = exerciseRepository.findDefaultOrCustomWithFilter(
//                false, null, null, null, true, List.of(bodyPart1.getId(), bodyPart4.getId()), pageable);
//
//        // Then
//        assertEquals(2, exercisePage.getTotalElements());
//        assertEquals(1, exercisePage.getTotalPages());
//        assertEquals(2, exercisePage.getContent().size());
//        assertEquals(2, exercisePage.getContent().size());
//        assertEquals(currentPageNumber, exercisePage.getNumber());
//        assertEquals(itemsPerPage, exercisePage.getSize());
//
//        assertThat(exercisePage.getContent())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(List.of(defaultExercise1, defaultExercise2));
//    }
//
//    @ParameterizedTest
//    @MethodSource("findDefaultOrCustomWithFilter_multipleCustomFilters")
//    void findDefaultOrCustomWithFilterTest_shouldReturnCustomFilteredExercises(
//            String title,
//            String description,
//            Boolean needsEquipment,
//            int itemsPerPage,
//            int currentPageNumber,
//            int totalElements,
//            int totalPages,
//            int numberOfElementsCurrentPage,
//            List<Long> resultSeeds) {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
//        List<Long> bodyPartsIds =
//                bodyPartRepository.findAll().stream().map(BodyPart::getId).toList();
//
//        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
//
//        Exercise defaultExercise1 =
//                dbUtil.createDefaultExercise(0, true, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef1));
//        Exercise defaultExercise2 =
//                dbUtil.createDefaultExercise(1, true, List.of(bodyPart3, bodyPart4), List.of(defaultHttpRef1));
//        Exercise defaultExercise3 =
//                dbUtil.createDefaultExercise(2, false, List.of(bodyPart3), List.of(defaultHttpRef1));
//        Exercise defaultExercise4 =
//                dbUtil.createDefaultExercise(3, false, List.of(bodyPart4), List.of(defaultHttpRef1));
//
//        Role role = dbUtil.createUserRole();
//        Country country = dbUtil.createCountry(1);
//        User user1 = dbUtil.createUser(1, role, country);
//        User user2 = dbUtil.createUser(2, role, country);
//
//        Exercise customExercise1User1 =
//                dbUtil.createCustomExercise(4, true, List.of(bodyPart1), List.of(defaultHttpRef1), user1);
//        Exercise customExercise2User1 =
//                dbUtil.createCustomExercise(5, true, List.of(bodyPart2), List.of(defaultHttpRef1), user1);
//        Exercise customExercise3User1 =
//                dbUtil.createCustomExercise(6, false, List.of(bodyPart3), List.of(defaultHttpRef1), user1);
//        Exercise customExercise4User1 =
//                dbUtil.createCustomExercise(7, false, List.of(bodyPart4), List.of(defaultHttpRef1), user1);
//
//        Exercise customExercise1User2 =
//                dbUtil.createCustomExercise(8, true, List.of(bodyPart1), List.of(defaultHttpRef1), user2);
//        Exercise customExercise2User2 =
//                dbUtil.createCustomExercise(9, false, List.of(bodyPart3), List.of(defaultHttpRef1), user2);
//
//        List<Exercise> expectedFilteredExercises = Stream.of(
//                        defaultExercise1,
//                        defaultExercise2,
//                        defaultExercise3,
//                        defaultExercise4,
//                        customExercise1User1,
//                        customExercise2User1,
//                        customExercise3User1,
//                        customExercise4User1,
//                        customExercise1User2,
//                        customExercise2User2)
//                .filter(exercise -> resultSeeds.stream()
//                        .anyMatch(seed -> exercise.getTitle().contains(String.valueOf(seed))))
//                .toList();
//
//        String direction = "ASC";
//        String sortBy = "id";
//        Pageable pageable =
//                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction),
// sortBy));
//
//        // When
//        Page<Exercise> exercisePage = exerciseRepository.findDefaultOrCustomWithFilter(
//                true, user1.getId(), title, description, needsEquipment, bodyPartsIds, pageable);
//
//        // Then
//        assertEquals(totalElements, exercisePage.getTotalElements());
//        assertEquals(totalPages, exercisePage.getTotalPages());
//        assertEquals(resultSeeds.size(), exercisePage.getContent().size());
//        assertEquals(numberOfElementsCurrentPage, exercisePage.getContent().size());
//        assertEquals(currentPageNumber, exercisePage.getNumber());
//        assertEquals(itemsPerPage, exercisePage.getSize());
//
//        assertThat(exercisePage.getContent())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(expectedFilteredExercises);
//    }
//
//    static Stream<Arguments> findDefaultOrCustomWithFilter_multipleCustomFilters() {
//        return Stream.of(
//                // Custom, positive
//                Arguments.of(null, null, true, 2, 0, 2, 1, 2, List.of(4L, 5L)),
//                Arguments.of("exercise", null, true, 2, 0, 2, 1, 2, List.of(4L, 5L)),
//                Arguments.of(null, "desc", true, 2, 0, 2, 1, 2, List.of(4L, 5L)),
//                Arguments.of("exercise", "desc", true, 2, 0, 2, 1, 2, List.of(4L, 5L)),
//                Arguments.of(null, null, false, 2, 0, 2, 1, 2, List.of(6L, 7L)),
//                Arguments.of("exercise", null, false, 2, 0, 2, 1, 2, List.of(6L, 7L)),
//                Arguments.of(null, "desc", false, 2, 0, 2, 1, 2, List.of(6L, 7L)),
//                Arguments.of("exercise", "desc", false, 2, 0, 2, 1, 2, List.of(6L, 7L)),
//                Arguments.of(null, null, null, 4, 0, 4, 1, 4, List.of(4L, 5L, 6L, 7L)),
//                Arguments.of("exercise", null, null, 4, 0, 4, 1, 4, List.of(4L, 5L, 6L, 7L)),
//                Arguments.of(null, "desc", null, 4, 0, 4, 1, 4, List.of(4L, 5L, 6L, 7L)),
//                Arguments.of("exercise", "desc", null, 4, 0, 4, 1, 4, List.of(4L, 5L, 6L, 7L)),
//
//                // Custom, empty
//                Arguments.of("non-existent", null, true, 2, 0, 0, 0, 0, Collections.emptyList()),
//                Arguments.of(null, "non-existent", false, 2, 0, 0, 0, 0, Collections.emptyList()));
//    }
//
//    @ParameterizedTest
//    @MethodSource("findDefaultAndCustomWithFilter_multipleFilters")
//    void findDefaultAndCustomWithFilterTest_shouldReturnFilteredExercises(
//            String title,
//            String description,
//            Boolean needsEquipment,
//            int itemsPerPage,
//            int currentPageNumber,
//            int totalElements,
//            int totalPages,
//            int numberOfElementsCurrentPage,
//            List<Long> resultSeeds) {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
//        List<Long> bodyPartsIds =
//                bodyPartRepository.findAll().stream().map(BodyPart::getId).toList();
//
//        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
//
//        Exercise defaultExercise1 =
//                dbUtil.createDefaultExercise(0, true, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef1));
//        Exercise defaultExercise2 =
//                dbUtil.createDefaultExercise(1, true, List.of(bodyPart3, bodyPart4), List.of(defaultHttpRef1));
//        Exercise defaultExercise3 =
//                dbUtil.createDefaultExercise(2, false, List.of(bodyPart3), List.of(defaultHttpRef1));
//        Exercise defaultExercise4 =
//                dbUtil.createDefaultExercise(3, false, List.of(bodyPart4), List.of(defaultHttpRef1));
//
//        Role role = dbUtil.createUserRole();
//        Country country = dbUtil.createCountry(1);
//        User user1 = dbUtil.createUser(1, role, country);
//        User user2 = dbUtil.createUser(2, role, country);
//
//        Exercise customExercise1User1 =
//                dbUtil.createCustomExercise(4, true, List.of(bodyPart1), List.of(defaultHttpRef1), user1);
//        Exercise customExercise2User1 =
//                dbUtil.createCustomExercise(5, true, List.of(bodyPart2), List.of(defaultHttpRef1), user1);
//        Exercise customExercise3User1 =
//                dbUtil.createCustomExercise(6, false, List.of(bodyPart3), List.of(defaultHttpRef1), user1);
//        Exercise customExercise4User1 =
//                dbUtil.createCustomExercise(7, false, List.of(bodyPart4), List.of(defaultHttpRef1), user1);
//
//        Exercise customExercise1User2 =
//                dbUtil.createCustomExercise(8, true, List.of(bodyPart1), List.of(defaultHttpRef1), user2);
//        Exercise customExercise2User2 =
//                dbUtil.createCustomExercise(9, false, List.of(bodyPart3), List.of(defaultHttpRef1), user2);
//
//        List<Exercise> expectedFilteredExercises = Stream.of(
//                        defaultExercise1,
//                        defaultExercise2,
//                        defaultExercise3,
//                        defaultExercise4,
//                        customExercise1User1,
//                        customExercise2User1,
//                        customExercise3User1,
//                        customExercise4User1,
//                        customExercise1User2,
//                        customExercise2User2)
//                .filter(exercise -> resultSeeds.stream()
//                        .anyMatch(seed -> exercise.getTitle().contains(String.valueOf(seed))))
//                .toList();
//
//        String direction = "ASC";
//        String sortBy = "id";
//        Pageable pageable =
//                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction),
// sortBy));
//
//        // When
//        Page<Exercise> exercisePage = exerciseRepository.findDefaultAndCustomWithFilter(
//                user1.getId(), title, description, needsEquipment, bodyPartsIds, pageable);
//
//        // Then
//        assertEquals(totalElements, exercisePage.getTotalElements());
//        assertEquals(totalPages, exercisePage.getTotalPages());
//        assertEquals(resultSeeds.size(), exercisePage.getContent().size());
//        assertEquals(numberOfElementsCurrentPage, exercisePage.getContent().size());
//        assertEquals(currentPageNumber, exercisePage.getNumber());
//        assertEquals(itemsPerPage, exercisePage.getSize());
//
//        assertThat(exercisePage.getContent())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(expectedFilteredExercises);
//    }
//
//    static Stream<Arguments> findDefaultAndCustomWithFilter_multipleFilters() {
//        return Stream.of(
//                // Positive
//                Arguments.of(null, null, true, 4, 0, 4, 1, 4, List.of(0L, 1L, 4L, 5L)),
//                Arguments.of(null, null, false, 4, 0, 4, 1, 4, List.of(2L, 3L, 6L, 7L)),
//                Arguments.of(null, null, null, 8, 0, 8, 1, 8, List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L)),
//                Arguments.of("exercise", null, true, 4, 0, 4, 1, 4, List.of(0L, 1L, 4L, 5L)),
//                Arguments.of("exercise", null, false, 4, 0, 4, 1, 4, List.of(2L, 3L, 6L, 7L)),
//                Arguments.of("exercise", null, null, 8, 0, 8, 1, 8, List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L)),
//
//                // Empty
//                Arguments.of("non-existent", null, true, 2, 0, 0, 0, 0, Collections.emptyList()),
//                Arguments.of(null, "non-existent", false, 2, 0, 0, 0, 0, Collections.emptyList()));
//    }
//
//    @Test
//    void findDefaultOrCustomWithFilterTest_shouldReturnCustomFilteredExercises_whenBodyPartIdsGiven() {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
//        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
//
//        Exercise defaultExercise1 =
//                dbUtil.createDefaultExercise(0, true, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef1));
//        Exercise defaultExercise2 =
//                dbUtil.createDefaultExercise(1, true, List.of(bodyPart3, bodyPart4), List.of(defaultHttpRef1));
//        Exercise defaultExercise3 =
//                dbUtil.createDefaultExercise(2, false, List.of(bodyPart3), List.of(defaultHttpRef1));
//        Exercise defaultExercise4 =
//                dbUtil.createDefaultExercise(3, false, List.of(bodyPart4), List.of(defaultHttpRef1));
//
//        Role role = dbUtil.createUserRole();
//        Country country = dbUtil.createCountry(1);
//        User user1 = dbUtil.createUser(1, role, country);
//        User user2 = dbUtil.createUser(2, role, country);
//
//        Exercise customExercise1User1 =
//                dbUtil.createCustomExercise(4, true, List.of(bodyPart1), List.of(defaultHttpRef1), user1);
//        Exercise customExercise2User1 =
//                dbUtil.createCustomExercise(5, true, List.of(bodyPart2), List.of(defaultHttpRef1), user1);
//        Exercise customExercise3User1 =
//                dbUtil.createCustomExercise(6, false, List.of(bodyPart3), List.of(defaultHttpRef1), user1);
//        Exercise customExercise4User1 =
//                dbUtil.createCustomExercise(7, false, List.of(bodyPart4), List.of(defaultHttpRef1), user1);
//
//        Exercise customExercise1User2 =
//                dbUtil.createCustomExercise(8, true, List.of(bodyPart1), List.of(defaultHttpRef1), user2);
//        Exercise customExercise2User2 =
//                dbUtil.createCustomExercise(9, false, List.of(bodyPart3), List.of(defaultHttpRef1), user2);
//
//        String direction = "ASC";
//        String sortBy = "id";
//        int currentPageNumber = 0;
//        int itemsPerPage = 2;
//        Pageable pageable =
//                PageRequest.of(currentPageNumber, itemsPerPage, Sort.by(Sort.Direction.fromString(direction),
// sortBy));
//
//        // When
//        Page<Exercise> exercisePage = exerciseRepository.findDefaultOrCustomWithFilter(
//                true, user1.getId(), null, null, true, List.of(bodyPart1.getId(), bodyPart2.getId()), pageable);
//
//        // Then
//        assertEquals(2, exercisePage.getTotalElements());
//        assertEquals(1, exercisePage.getTotalPages());
//        assertEquals(2, exercisePage.getContent().size());
//        assertEquals(2, exercisePage.getContent().size());
//        assertEquals(currentPageNumber, exercisePage.getNumber());
//        assertEquals(itemsPerPage, exercisePage.getSize());
//
//        assertThat(exercisePage.getContent())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(List.of(customExercise1User1, customExercise2User1));
//    }
//
//    @Test
//    void findCustomByTitleAndUserIdTest_shouldReturnCustomExercise() {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//
//        User user = dbUtil.createUser(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
//
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);
//
//        // When
//        Optional<Exercise> actualExerciseOpt =
//                exerciseRepository.findCustomByTitleAndUserId(customExercise1.getTitle(), user.getId());
//
//        // Then
//        assertTrue(actualExerciseOpt.isPresent());
//        Exercise actualExercise = actualExerciseOpt.get();
//        assertEquals(customExercise1.getId(), actualExercise.getId());
//    }
//
//    @Test
//    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenWrongTitleGiven() {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//
//        User user = dbUtil.createUser(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
//
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);
//        String nonExistentTitle = customExercise1.getTitle() + " non existent title";
//
//        // When
//        Optional<Exercise> actualOpt = exerciseRepository.findCustomByTitleAndUserId(nonExistentTitle, user.getId());
//
//        // Then
//        assertTrue(actualOpt.isEmpty());
//    }
//
//    @Test
//    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenWrongUserIdGiven() {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//
//        User user = dbUtil.createUser(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
//
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);
//
//        long nonExistentUserId = 1000L;
//
//        // When
//        Optional<Exercise> actualOpt =
//                exerciseRepository.findCustomByTitleAndUserId(customExercise1.getTitle(), nonExistentUserId);
//
//        // Then
//        assertTrue(actualOpt.isEmpty());
//    }
//
//    @Test
//    void findCustomByTitleAndUserId_shouldReturnOptionalEmpty_whenDefaultExerciseTitleGiven() {
//        // Given
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//
//        User user = dbUtil.createUser(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
//
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);
//
//        // When
//        Optional<Exercise> actualOpt =
//                exerciseRepository.findCustomByTitleAndUserId(defaultExercise.getTitle(), user.getId());
//
//        // Then
//        assertTrue(actualOpt.isEmpty());
//    }
//
//    @Test
//    void findCustomByExerciseIdAndUserIdTest_shouldReturnCustomExercise() {
//        // Given
//        Role role = dbUtil.createUserRole();
//        Country country = dbUtil.createCountry(1);
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//
//        User user = dbUtil.createUser(1, role, country);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
//        Exercise customExercise1 =
//                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
//
//        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
//        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user);
//        Exercise customExercise2 =
//                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef2), user);
//
//        User user2 = dbUtil.createUser(2, role, country);
//        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
//        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(3, user);
//        Exercise customExercise3 =
//                dbUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart3), List.of(customHttpRef3), user2);
//
//        Sort sort = Sort.by(Sort.Direction.ASC, "id");
//
//        // When
//        Optional<Exercise> exercisesOptional =
//                exerciseRepository.findCustomByExerciseIdAndUserId(customExercise1.getId(), user.getId());
//
//        // Then
//        assertTrue(exercisesOptional.isPresent());
//        Exercise exerciseActual = exercisesOptional.get();
//        assertThat(exerciseActual)
//                .usingRecursiveComparison()
//                .ignoringFields("httpRefs", "bodyParts", "user")
//                .isEqualTo(customExercise1);
//    }
//
//    @Test
//    void findCustomByExerciseIdAndUserIdTest_shouldReturnOptionalEmpty_whenInvalidIdGiven() {
//        long nonExistingExerciseId = 1000L;
//        long nonExistingUserId = 1000L;
//
//        // When
//        Optional<Exercise> exerciseOptional =
//                exerciseRepository.findCustomByExerciseIdAndUserId(nonExistingExerciseId, nonExistingUserId);
//
//        // Then
//        assertTrue(exerciseOptional.isEmpty());
//    }
//
//    @Test
//    void findCustomByExerciseIdAndUserIdTest_shouldReturnOptionalEmpty_whenUserExerciseMismatch() {
//        // Given
//        Role role = dbUtil.createUserRole();
//        Country country = dbUtil.createCountry(1);
//        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
//        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
//        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
//        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
//
//        User user1 = dbUtil.createUser(1, role, country);
//        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
//        Exercise customExercise1 = dbUtil.createCustomExercise(
//                1, true, List.of(bodyPart1), List.of(defaultHttpRef1, customHttpRef1), user1);
//
//        User user2 = dbUtil.createUser(2, role, country);
//        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);
//        Exercise customExercise2 = dbUtil.createCustomExercise(
//                2, true, List.of(bodyPart2), List.of(defaultHttpRef2, customHttpRef2), user1);
//
//        // When
//        Optional<Exercise> exerciseOptional =
//                exerciseRepository.findCustomByExerciseIdAndUserId(customExercise1.getId(), user2.getId());
//
//        // Then
//        assertTrue(exerciseOptional.isEmpty());
//    }
// }
