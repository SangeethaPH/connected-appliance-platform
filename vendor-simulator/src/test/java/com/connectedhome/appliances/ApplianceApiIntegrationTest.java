package com.connectedhome.appliances;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApplianceApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createsApplianceEmitsAndReadsMetrics() throws Exception {
        String response = mockMvc.perform(post("/api/simulator/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendorId":"acme","name":"Kitchen Fridge","type":"REFRIGERATOR","metricIntervalSeconds":30,"emissionMode":"MANUAL_ONLY"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ONLINE"))
                .andExpect(jsonPath("$.emissionMode").value("MANUAL_ONLY"))
                .andReturn().getResponse().getContentAsString();

        JsonNode appliance = objectMapper.readTree(response);
        String id = appliance.get("id").asText();

        mockMvc.perform(post("/api/simulator/appliances/{id}/emit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.power_watts").isNumber())
                .andExpect(jsonPath("$.metrics.internal_temp_c").isNumber());

        String metrics = mockMvc.perform(get("/api/simulator/appliances/{id}/metrics", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(metrics)).hasSize(2);
    }

    @Test
    void rejectsUnknownVendor() throws Exception {
        mockMvc.perform(post("/api/simulator/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendorId":"missing","name":"TV","type":"TELEVISION","metricIntervalSeconds":5}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointIsAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
