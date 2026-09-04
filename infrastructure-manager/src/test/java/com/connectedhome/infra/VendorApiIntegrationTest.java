package com.connectedhome.infra;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VendorApiIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void rejectsUnauthenticatedManagementRequest() throws Exception {
        mockMvc.perform(get("/api/vendors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsAndListsVendorWithoutExposingPassword() throws Exception {
        mockMvc.perform(post("/api/vendors")
                        .with(httpBasic("admin", "test-only-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"acme",
                                  "name":"Acme Smart Home",
                                  "baseUrl":"http://localhost:8081",
                                  "username":"test-vendor-user",
                                  "password":"test-only-vendor-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("acme"))
                .andExpect(jsonPath("$.authenticationType").value("BASIC"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
