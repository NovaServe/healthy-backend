package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.*;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.Exercise;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import({DataConfiguration.class})
public class ExerciseControllerUpdateTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Autowired
    DataUtil dataUtil;

    @Autowired
    UserJpaTestBuilder userJpaTestBuilder;

    @Autowired
    ExerciseDtoTestBuilder exerciseDtoTestBuilder;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String URL = "/api/v1/workouts/exercises";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDtoAnd200_whenAllFieldsAreBeingUpdated() {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserSeed(1)
                .setRoleUser()
                .setExerciseSeed(1)
                .setExerciseCustom(true)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(4)
                .setStartSeedForExerciseNestedEntities(1)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestWrapper
                .setSeed("Update")
                .setNeedsEquipment(false)
                .setMediasIds(Collections.emptyList())
                .setBodyPartsIds(Collections.emptyList())
                .buildUpdateExerciseDto();

        // Under the test, initial state
        Exercise exerciseInitialState = userWrapper.getSingleExercise();
        Exercise exerciseExpectedFinalState;
        // When

        // Then
        // Under the test, final state
        Exercise exerciseActualFinalState;

        // Method behavior during the test
        assertTrue(true);
    }

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDtoAnd200_whenValidTitleAreBeingUpdated() {}

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDtoAnd200_whenValidDescriptionAreBeingUpdated() {}

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDtoAnd200_whenValidMediaRefsAreBeingUpdated() {}

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDtoAnd200_whenValidEmptyListMediaRefsAreBeingUpdated() {}

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDtoAnd200_whenValidBodyPartsAreBeingUpdated() {}

    @Test
    void updateCustomExerciseTest_shouldReturnValidationMessageAnd400_whenInvalidTitleTryToUpdate() {}

    @Test
    void updateCustomExerciseTest_shouldReturnValidationMessageAnd400_whenInvalidDescriptionTryToUpdate() {}

    @Test
    void updateCustomExerciseTest_shouldReturnValidationMessageAnd400_whenNullBodyPartsProvided() {}

    @Test
    void updateCustomExerciseTest_shouldReturnValidationMessageAnd400_whenEmptyBodyPartsProvided() {}

    @Test
    void updateCustomExerciseTest_shouldReturnValidationMessageAnd400_whenNullMediaRefsProvided() {}

    @Test
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenEmptyRequestDtoProvided() {
        // Given

        // When

        // Then
    }

    @Test
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd404_whenExerciseNotFound() {
        // Given

        // When

        // Then
    }

    @Test
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenExerciseDoesntBelongToUser() {
        // Given

        // When

        // Then
    }

    @Test
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenExerciseWithNewTitleAlreadyExists() {
        // Given

        // When

        // Then
    }

    @Test
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenBodyPartNotFound() {
        // Given

        // When

        // Then
    }

    @Test
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenMediaRefDoesntBelongToUser() {
        // Given

        // When

        // Then
    }
}
