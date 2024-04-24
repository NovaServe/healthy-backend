package healthy.lifestyle.backend.activity.mental.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    FirebaseMessaging firebaseMessaging;

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
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("mentals")
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
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users", "mentals")
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

    @Test
    void getAllMentals_shouldReturnListDefaultMentalsWith200_whenValidRequest() throws Exception {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        String sortDirection = "ASC";
        String sortField = "title";

        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType2);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef2), mentalType1);
        Mental customMental = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user);

        List<Mental> expectedMentalList = Stream.of(defaultMental1, defaultMental2, defaultMental3, defaultMental4)
                .sorted(Comparator.comparingLong(Mental::getId))
                .toList();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.ALL_MENTALS)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(expectedMentalList.size())))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> responseDto =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(expectedMentalList);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getAllMentals_shouldReturnListMentalsDtoWith200_whenValidRequest() throws Exception {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        String sortDirection = "ASC";
        String sortField = "title";

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType2);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef2), mentalType1);

        Mental customMental1 = dbUtil.createCustomMental(5, List.of(customHttpRef1), mentalType1, user1);
        Mental customMental2 =
                dbUtil.createCustomMental(6, List.of(defaultHttpRef1, customHttpRef1), mentalType2, user1);
        Mental customMental3 =
                dbUtil.createCustomMental(7, List.of(defaultHttpRef2, customHttpRef1), mentalType2, user1);
        Mental customMental4 = dbUtil.createCustomMental(8, List.of(customHttpRef2), mentalType1, user2);
        Mental customMental5 =
                dbUtil.createCustomMental(9, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user2);

        List<Mental> expectedMentalList = Stream.of(
                        defaultMental1,
                        defaultMental2,
                        defaultMental3,
                        defaultMental4,
                        customMental1,
                        customMental2,
                        customMental3)
                .sorted(Comparator.comparingLong(Mental::getId))
                .toList();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.ALL_MENTALS)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(expectedMentalList.size())))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> responseDto =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(expectedMentalList);
    }
}
