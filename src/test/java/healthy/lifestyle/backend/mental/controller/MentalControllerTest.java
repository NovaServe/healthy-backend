package healthy.lifestyle.backend.mental.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.mental.dto.MentalTypeResponseDto;
import healthy.lifestyle.backend.mental.model.Mental;
import healthy.lifestyle.backend.mental.model.MentalType;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.URL;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
public class MentalControllerTest {
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
    void getDefaultMentalById_shouldReturnDefaultMentalDtoWith200_whenValidRequest() throws Exception {
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

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.DEFAULT_MENTAL_ID, defaultMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "user", "mentalTypeId")
                .isEqualTo(defaultMental1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("mentals", "user")
                .isEqualTo(defaultMental1.getHttpRefsSortedById());
    }

    @Test
    void getDefaultMentalById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentDefaultMentalId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentDefaultMentalId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.DEFAULT_MENTAL_ID, nonExistentDefaultMentalId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalById_shouldReturnDtoWith200_whenValidRequest() throws Exception {
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

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.CUSTOM_MENTAL_ID, customMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(customMental1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "mentals")
                .isEqualTo(customMental1.getHttpRefsSortedById());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentCustomMentalId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentCustomMentalId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_ID, nonExistentCustomMentalId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalById_shouldReturnErrorMessageWith400_whenMentalUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental = dbUtil.createCustomMental(3, List.of(defaultHttpRef1), mentalType1, user2);
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, customMental.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_ID, customMental.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("getMentalsWithFilter_multipleDefaultFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalsWithFilterTest_shouldReturnDefaultFilteredMentalsWith200_whenValidFilters(
            String title,
            String description,
            String mentalTypeName,
            int pageSize,
            int pageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds)
            throws Exception {
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
        Mental customMental2 = dbUtil.createCustomMental(6, List.of(defaultHttpRef2), mentalType2, user);
        Mental customMental3 =
                dbUtil.createCustomMental(7, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        List<Mental> expectedFilteredMentals = Stream.of(
                        defaultMental1,
                        defaultMental2,
                        defaultMental3,
                        defaultMental4,
                        customMental1,
                        customMental2,
                        customMental3)
                .filter(mental -> resultSeeds.stream()
                        .anyMatch(seed -> mental.getTitle().equals(String.format("Mental %d", seed))))
                .toList();

        String sortDirection = "ASC";
        String sortField = "id";

        Optional<String> mentalTypeFilter = Stream.of(mentalType1, mentalType2)
                .filter(type -> type.getName().equals(mentalTypeName))
                .map(type -> type.getId().toString())
                .findFirst();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL)
                        .param("isCustom", String.valueOf(false))
                        .param("title", title)
                        .param("description", description)
                        .param("mentalTypeId", mentalTypeFilter.orElse(null))
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElementsCurrentPage)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "httpRefs", "type", "mentalTypeId")
                .isEqualTo(expectedFilteredMentals);
    }

    static Stream<Arguments> getMentalsWithFilter_multipleDefaultFilters() {
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

                // Default, empty
                Arguments.of("non-existent", null, "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non-existent", "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @ParameterizedTest
    @MethodSource("getMentalsWithFilter_multipleCustomFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getEMentalsWithFilterTest_shouldReturnCustomFilteredMentalsWith200_whenValidFilters(
            String title,
            String description,
            String mentalTypeName,
            int pageSize,
            int pageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds)
            throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef1), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental1User1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user1);
        Mental customMental2User1 = dbUtil.createCustomMental(6, List.of(defaultHttpRef2), mentalType2, user1);
        Mental customMental3User1 =
                dbUtil.createCustomMental(7, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user1);
        Mental customMental4User1 = dbUtil.createCustomMental(8, List.of(defaultHttpRef2), mentalType2, user1);

        Mental customMental5User2 =
                dbUtil.createCustomMental(9, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user2);
        Mental customMental6User2 =
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
                        customMental5User2,
                        customMental6User2)
                .filter(mental -> resultSeeds.stream()
                        .anyMatch(seed -> mental.getTitle().equals(String.format("Mental %d", seed))))
                .toList();

        String sortDirection = "ASC";
        String sortField = "id";
        Optional<String> mentalTypeFilter = Stream.of(mentalType1, mentalType2)
                .filter(mentalType -> mentalType.getName().equals(mentalTypeName))
                .map(mentalType -> mentalType.getId().toString())
                .findFirst();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL)
                        .param("isCustom", String.valueOf(true))
                        .param("title", title)
                        .param("description", description)
                        .param("mentalTypeId", mentalTypeFilter.orElse(null))
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElementsCurrentPage)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "httpRefs", "type", "mentalTypeId")
                .isEqualTo(expectedFilteredMentals);
    }

    static Stream<Arguments> getMentalsWithFilter_multipleCustomFilters() {
        return Stream.of(
                // Custom, positive
                Arguments.of(null, null, "MEDITATION", 2, 0, 2, 1, 2, List.of(6L, 8L)),
                Arguments.of("mental", null, "MEDITATION", 2, 0, 2, 1, 2, List.of(6L, 8L)),
                Arguments.of(null, "desc", "MEDITATION", 2, 0, 2, 1, 2, List.of(6L, 8L)),
                Arguments.of("mental", "desc", "MEDITATION", 2, 0, 2, 1, 2, List.of(6L, 8L)),
                Arguments.of(null, null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(5L, 7L)),
                Arguments.of("mental", null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(5L, 7L)),
                Arguments.of(null, "desc", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(5L, 7L)),
                Arguments.of("mental", "desc", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(5L, 7L)),

                // Custom, empty
                Arguments.of("non-existent", null, "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non-existent", "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalsWithFilterTest_shouldReturnDefaultAndCustomFilteredMentalsWith200() throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef1), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental1User1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user1);
        Mental customMental2User1 = dbUtil.createCustomMental(6, List.of(defaultHttpRef2), mentalType2, user1);
        Mental customMental3User1 =
                dbUtil.createCustomMental(7, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user1);
        Mental customMental4User1 = dbUtil.createCustomMental(8, List.of(defaultHttpRef2), mentalType2, user1);

        Mental customMental5User2 =
                dbUtil.createCustomMental(9, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user2);
        Mental customMental6User2 =
                dbUtil.createCustomMental(10, List.of(defaultHttpRef2, defaultHttpRef3), mentalType2, user2);

        List<Mental> expectedFilteredMentals = List.of(
                defaultMental1,
                defaultMental2,
                defaultMental3,
                defaultMental4,
                customMental1User1,
                customMental2User1,
                customMental3User1,
                customMental4User1);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(8)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(8)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});
        assertEquals(8, mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "httpRefs", "type", "mentalTypeId")
                .isEqualTo(expectedFilteredMentals);
    }

    @Test
    void getMentalTypeTest_shouldReturnMentalTypesWith200_whenValidRequest() throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createMentalType(1);
        MentalType mentalType2 = dbUtil.createMentalType(2);
        MentalType mentalType3 = dbUtil.createMentalType(3);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.MENTAL_TYPE).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<MentalTypeResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<MentalTypeResponseDto>>() {});

        assertEquals(3, responseDto.size());
        assertEquals(mentalType1.getId(), responseDto.get(0).getId());
        assertEquals(mentalType1.getName(), responseDto.get(0).getName());
        assertEquals(mentalType2.getId(), responseDto.get(1).getId());
        assertEquals(mentalType2.getName(), responseDto.get(1).getName());
        assertEquals(mentalType3.getId(), responseDto.get(2).getId());
        assertEquals(mentalType3.getName(), responseDto.get(2).getName());
    }

    @Test
    void getMentalTypeTest_shouldReturnErrorMessageWith500_whenNoMentalTypes() throws Exception {
        // Given
        ApiException expectedException = new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.MENTAL_TYPE).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }
}
