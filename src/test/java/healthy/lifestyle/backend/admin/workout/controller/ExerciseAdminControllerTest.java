package healthy.lifestyle.backend.admin.workout.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.URL;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(BeanConfig.class)
class ExerciseAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DbUtil dbUtil;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("multipleFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "ADMIN")
    void getExercisesByFilterTest_shouldReturnFilteredExercisesDtoListWith200_whenValidRequest(
            String title, String description, Boolean isCustom, Boolean needsEquipment, List<Integer> resultSeeds)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);

        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        Exercise defaultExercise1 = dbUtil.createDefaultExercise(1, true, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(2, false, List.of(bodyPart2), List.of(defaultHttpRef2));

        Exercise customExercise1 = dbUtil.createCustomExercise(
                3, true, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef1, customHttpRef1), user);
        Exercise customExercise2 = dbUtil.createCustomExercise(
                4, false, List.of(bodyPart1, bodyPart2), List.of(defaultHttpRef2, customHttpRef2), user);

        // When
        MockHttpServletRequestBuilder requestBuilder = get(URL.ADMIN_EXERCISES)
                .contentType(MediaType.APPLICATION_JSON)
                .param("title", title)
                .param("description", description);

        if (isCustom != null) requestBuilder.param("isCustom", String.valueOf(isCustom));
        if (needsEquipment != null) requestBuilder.param("needsEquipment", String.valueOf(needsEquipment));

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<ExerciseResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<ExerciseResponseDto>>() {});

        Assertions.assertEquals(responseDto.size(), resultSeeds.size());

        for (ExerciseResponseDto dto : responseDto) {
            if (title != null) Assertions.assertEquals(title, dto.getTitle());
            if (description != null) Assertions.assertEquals(description, dto.getDescription());
            if (isCustom != null) Assertions.assertEquals(isCustom, dto.isCustom());
            if (needsEquipment != null) Assertions.assertEquals(needsEquipment, dto.isNeedsEquipment());
        }
    }

    static Stream<Arguments> multipleFilters() {
        return Stream.of(
                // Positive cases for default exercises
                Arguments.of(null, null, false, null, List.of(1, 2)),
                Arguments.of("Exercise 1", null, false, null, List.of(1)),
                Arguments.of(null, "Desc 1", false, true, List.of(1)),
                Arguments.of("Exercise 2", "Desc 2", false, false, List.of(2)),
                Arguments.of("Exercise 2", null, false, null, List.of(2)),

                // Negative cases for default exercises
                Arguments.of("NonExistentValue", "NonExistentValue", false, null, Collections.emptyList()),
                Arguments.of(null, "NonExistentValue", false, true, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, false, false, Collections.emptyList()),

                // Positive cases for custom exercises
                Arguments.of(null, null, true, null, List.of(3, 4)),
                Arguments.of("Exercise 3", null, true, null, List.of(3)),
                Arguments.of(null, "Desc 3", true, true, List.of(3)),
                Arguments.of("Exercise 4", "Desc 4", true, false, List.of(4)),
                Arguments.of("Exercise 4", null, true, null, List.of(4)),

                // Negative cases for custom exercises
                Arguments.of("NonExistentValue", "NonExistentValue", true, null, Collections.emptyList()),
                Arguments.of(null, "NonExistentValue", true, true, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, true, false, Collections.emptyList()),

                // Positive cases for all exercises
                Arguments.of(null, null, null, null, List.of(1, 2, 3, 4)),

                // Negative cases for all exercises
                Arguments.of("NonExistentValue", "NonExistentValue", null, null, Collections.emptyList()),
                Arguments.of(null, "NonExistentValue", null, null, Collections.emptyList()),
                Arguments.of("NonExistentValue", null, null, null, Collections.emptyList()));
    }
}
