package healthy.lifestyle.backend.nutritions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.nutrition.dto.NutritionResponseDto;
import healthy.lifestyle.backend.nutrition.model.Nutrition;
import healthy.lifestyle.backend.nutrition.model.NutritionType;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.URL;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
@Import(BeanConfig.class)
public class NutritionControllerTest {
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

    @Test
    void getDefaultNutritionByIdTest_shouldReturnDefaultNutritionDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        NutritionType nutritionType1 = dbUtil.createSupplementType();
        NutritionType nutritionType2 = dbUtil.createRecipeType();

        Nutrition defaultNutrition1 = dbUtil.createDefaultNutrition(1, List.of(defaultHttpRef1), nutritionType1);
        Nutrition defaultNutrition2 = dbUtil.createDefaultNutrition(2, List.of(defaultHttpRef2), nutritionType2);

        User user = dbUtil.createUser(1);

        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        Nutrition customNutrition1 = dbUtil.createCustomNutrition(3, List.of(customHttpRef1), nutritionType1, user);
        Nutrition customNutrition2 = dbUtil.createCustomNutrition(4, List.of(customHttpRef2), nutritionType2, user);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.DEFAULT_NUTRITION_ID, defaultNutrition1.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        NutritionResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<NutritionResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "user", "nutritionTypeId")
                .isEqualTo(defaultNutrition1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("nutritions")
                .isEqualTo(defaultNutrition1.getHttpRefsSortedById());
    }

    @Test
    void getDefaultNutritionByIdTest_shouldReturnErrorMessageWith404_whenDefaultNutritionNotFound() throws Exception {
        // Given
        long nonExistentDefaultNutritionId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.NUTRITION_NOT_FOUND, nonExistentDefaultNutritionId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.DEFAULT_NUTRITION_ID, nonExistentDefaultNutritionId)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }
}
