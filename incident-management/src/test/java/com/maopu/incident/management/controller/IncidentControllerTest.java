package com.maopu.incident.management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maopu.incident.management.entity.Incident;
import com.maopu.incident.management.response.ResponseFactory;
import com.maopu.incident.management.service.IncidentService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Incident sampleIncident;

    @BeforeEach
    public void setUp() {
        // 创建一个示例事件
        sampleIncident = new Incident();
        sampleIncident.setId(1L);
        sampleIncident.setTitle("title");
        sampleIncident.setDescription("description");
        sampleIncident.setStatus(Incident.IncidentStatus.OPEN);
        sampleIncident.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
    }

    private Page<Incident> createPageWithSampleIncident() {
        return new PageImpl<>(List.of(sampleIncident), PageRequest.of(0, 1), 1);
    }

    private String getJsonContent(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    public void testGetAllIncidents() throws Exception {
        // Setup
        when(incidentService.getPage(eq("title"), any(PageRequest.class)))
                .thenReturn(createPageWithSampleIncident());

        // Run the test
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/incidents")
                .param("title", "title")
                .param("page", "0")
                .param("size", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the results
        String expectedJson = getJsonContent(ResponseFactory.getSuccessData(createPageWithSampleIncident()));
        assertThat(result.getResponse().getContentAsString()).isEqualTo(expectedJson);
    }

    @Test
    public void testGetAllIncidents_NoItems() throws Exception {
        // Setup
        when(incidentService.getPage(eq("title"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 1), 0));

        // Run the test
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/incidents")
                .param("title", "title")
                .param("page", "0")
                .param("size", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the actual and expected JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode expectedJson = objectMapper.readTree(getJsonContent(ResponseFactory.getSuccessData(
                new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 1), 0)
        )));

        // Compare the JSON nodes, ignoring the pageable field
        JsonNode actualData = actualJson.get("data");
        JsonNode expectedData = expectedJson.get("data");

        Assertions.assertThat(actualJson.get("status")).isEqualTo(expectedJson.get("status"));
        Assertions.assertThat(actualJson.get("message")).isEqualTo(expectedJson.get("message"));
        Assertions.assertThat(actualData.get("content")).isEqualTo(expectedData.get("content"));
        Assertions.assertThat(actualData.get("totalElements")).isEqualTo(expectedData.get("totalElements"));
        Assertions.assertThat(actualData.get("totalPages")).isEqualTo(expectedData.get("totalPages"));
        Assertions.assertThat(actualData.get("last")).isEqualTo(expectedData.get("last"));
        Assertions.assertThat(actualData.get("size")).isEqualTo(expectedData.get("size"));
        Assertions.assertThat(actualData.get("number")).isEqualTo(expectedData.get("number"));
        Assertions.assertThat(actualData.get("first")).isEqualTo(expectedData.get("first"));
        Assertions.assertThat(actualData.get("numberOfElements")).isEqualTo(expectedData.get("numberOfElements"));
        Assertions.assertThat(actualData.get("empty")).isEqualTo(expectedData.get("empty"));
    }

    @Test
    public void testGetIncidentInfo() throws Exception {
        // Setup
        when(incidentService.findById(1L)).thenReturn(sampleIncident);

        // Run the test
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/incidents/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the results
        String expectedJson = getJsonContent(ResponseFactory.getSuccessData(sampleIncident));
        assertThat(result.getResponse().getContentAsString()).isEqualTo(expectedJson);
    }

    @Test
    public void testCreateIncident() throws Exception {
        // Setup
        when(incidentService.createIncident(any(Incident.class))).thenReturn(sampleIncident);

        // Run the test
        String jsonContent = getJsonContent(sampleIncident);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/incidents")
                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the results
        String expectedJson = getJsonContent(ResponseFactory.getSuccessData(sampleIncident));
        assertThat(result.getResponse().getContentAsString()).isEqualTo(expectedJson);
    }

    @Test
    public void testUpdateIncident() throws Exception {
        // Setup
        when(incidentService.updateIncident(eq(1L), any(Incident.class))).thenReturn(sampleIncident);

        // Run the test
        String jsonContent = getJsonContent(sampleIncident);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/incidents/1")
                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the results
        String expectedJson = getJsonContent(ResponseFactory.getSuccessData(sampleIncident));
        assertThat(result.getResponse().getContentAsString()).isEqualTo(expectedJson);
    }


    @Test
    public void testDeleteIncident() throws Exception {
        // Run the test
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/incidents/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the results
        String expectedJson = getJsonContent(ResponseFactory.getSuccess());
        assertThat(result.getResponse().getContentAsString()).isEqualTo(expectedJson);
    }
}