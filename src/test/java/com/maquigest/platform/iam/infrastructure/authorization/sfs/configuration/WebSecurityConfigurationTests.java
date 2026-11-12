package com.maquigest.platform.iam.infrastructure.authorization.sfs.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end security checks; the project's existing contextLoads test also needs MySQL. */
@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigurationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointRequiresBearerJwt() throws Exception {
        mockMvc.perform(get("/api/v1/authentication/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsForgedJwt() throws Exception {
        mockMvc.perform(get("/api/v1/authentication/me")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signInRemainsPublicWhenCredentialsAreInvalid() throws Exception {
        // An invalid password reaches the controller and returns 400, not security's 401.
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"unregistered@maquigest.test","password":"incorrect-password"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void swaggerDocumentationRemainsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
