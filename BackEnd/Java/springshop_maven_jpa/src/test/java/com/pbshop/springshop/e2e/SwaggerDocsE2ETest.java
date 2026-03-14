package com.pbshop.springshop.e2e;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class SwaggerDocsE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void openApiAndSwaggerUiAreExposed() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").exists());

        mockMvc.perform(get("/docs/swagger"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/docs/swagger-ui/index.html"));

        mockMvc.perform(get("/docs/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
