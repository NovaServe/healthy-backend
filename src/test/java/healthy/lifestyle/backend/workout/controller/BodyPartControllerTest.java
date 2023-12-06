package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.bodypart.BodyPartJpaTestBuilder;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
class BodyPartControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Autowired
    BodyPartJpaTestBuilder bodyPartJpaTestBuilder;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String URL = "/api/v1/workouts/bodyParts";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    void getBodyPartsTest_shouldReturnBodyPartsAndStatusOk() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<BodyPartResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<BodyPartResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(bodyPart1.getId(), responseDto.get(0).getId());
        assertEquals(bodyPart1.getName(), responseDto.get(0).getName());
        assertEquals(bodyPart2.getId(), responseDto.get(1).getId());
        assertEquals(bodyPart2.getName(), responseDto.get(1).getName());
    }

    @Test
    void getBodyPartsTest_shouldReturnBodyPartsDtoListAnd200_whenAllBodyPartsRequested() throws Exception {
        // Given
        BodyPartJpaTestBuilder.BodyPartWrapper bodyPartsWrapper = bodyPartJpaTestBuilder.getWrapper();
        bodyPartsWrapper.setIdOrSeed(1).setAmountOfEntities(2).buildList();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<BodyPartResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<BodyPartResponseDto>>() {});

        assertEquals(bodyPartsWrapper.size(), responseDto.size());
        assertThat(responseDto).usingRecursiveComparison().isEqualTo(bodyPartsWrapper.getAll());
    }

    @Test
    void getBodyPartsTest_shouldReturnErrorMessageAndStatusInternalServerError_whenNoBodyParts() throws Exception {
        // When
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Server error")))
                .andDo(print());
    }
}
