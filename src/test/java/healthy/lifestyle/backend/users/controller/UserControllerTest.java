package healthy.lifestyle.backend.users.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import healthy.lifestyle.backend.users.dto.UpdateUserRequestDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String COUNTRY_URL = "/api/v1/users/countries";
    private static final String UPDATE_USER_URL = "/api/v1/users/{userId}";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    void getAllCountriesTest_shouldReturnListOfCountriesAndStatusOk() throws Exception {
        // Given
        Country country1 = dataHelper.createCountry(1);
        Country country2 = dataHelper.createCountry(2);

        // When
        MvcResult mvcResult = mockMvc.perform(get(COUNTRY_URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<CountryResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<CountryResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(country1.getId(), responseDto.get(0).getId());
        assertEquals(country1.getName(), responseDto.get(0).getName());
        assertEquals(country2.getId(), responseDto.get(1).getId());
        assertEquals(country2.getName(), responseDto.get(1).getName());
    }

    @Test
    void getCountriesTest_shouldReturnErrorMessageAndStatusInternalServerError_whenNoCountries() throws Exception {
        // When
        mockMvc.perform(get(COUNTRY_URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Server error")))
                .andDo(print());
    }

    @Test
    @WithMockUser(
            username = "username-one",
            password = "password-one",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnUpdatedUserAndStatusOk() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        Integer age = 20;
        User user = dataHelper.createUser("one", role, country, null, age);
        Country updatedCountry = dataHelper.createCountry(2);
        UpdateUserRequestDto requestDto = dataHelper.createUpdateUserRequestDto("two", updatedCountry.getId(), 35);

        mockMvc.perform(patch(UPDATE_USER_URL, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(requestDto.getUpdatedUsername())))
                .andExpect(jsonPath("$.email", is(requestDto.getUpdatedEmail())))
                .andExpect(jsonPath("$.fullName", is(requestDto.getUpdatedFullName())))
                .andExpect(jsonPath("$.age", is(requestDto.getUpdatedAge())))
                .andExpect(jsonPath("$.countryId", is(updatedCountry.getId().intValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(
            username = "username-one",
            password = "password-one",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturn400_whenAuthenticationError() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        Integer age = 20;
        User user = dataHelper.createUser("one", role, country, null, age);
        User user2 = dataHelper.createUser("two", role, country, null, age);
        Country updatedCountry = dataHelper.createCountry(2);
        UpdateUserRequestDto requestDto = dataHelper.createUpdateUserRequestDto("two", updatedCountry.getId(), 35);

        mockMvc.perform(patch(UPDATE_USER_URL, user2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.AUTHENTICATION_ERROR.getName())))
                .andDo(print())
                .andReturn();
    }
}
