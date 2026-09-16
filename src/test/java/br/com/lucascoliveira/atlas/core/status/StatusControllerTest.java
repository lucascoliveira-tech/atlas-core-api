package br.com.lucascoliveira.atlas.core.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import br.com.lucascoliveira.atlas.core.config.SecurityConfiguration;

@WebMvcTest(StatusController.class)
@Import(SecurityConfiguration.class)
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reportsServiceAsAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("atlas-core-api"))
            .andExpect(jsonPath("$.status").value("UP"));
    }
}

