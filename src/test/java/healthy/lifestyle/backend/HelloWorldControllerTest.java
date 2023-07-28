package healthy.lifestyle.backend;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.common.E2EBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class HelloWorldControllerTest extends E2EBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URL = "/api/v1";

    @Test
    void helloWorld() throws Exception {
        mockMvc.perform(get(URL + "/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(equalTo("Hello World"))))
                .andDo(print());
    }
}
