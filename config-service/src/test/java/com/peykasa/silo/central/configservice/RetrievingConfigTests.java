package com.peykasa.silo.central.configservice;

import com.peykasa.configservice.RefreshController;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@SpringBootTest
@AutoConfigureMockMvc
class RetrievingConfigTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RefreshController eurekaClient;

    @SneakyThrows
    @Test
    @Order(1)
    public void getProfile_ShouldReturnTheProfile() {
        mockMvc.perform(MockMvcRequestBuilders.get("/test-app/dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("test-app")))
                .andExpect(jsonPath("$.profiles", contains("dev")))
                .andExpect(jsonPath("$.propertySources[0].source.foo", is("bar")));
    }

    @SneakyThrows
    @Test
    @Order(2)
    public void getNotExistProfile_ShouldReturnEmptyProfile() {
        mockMvc.perform(MockMvcRequestBuilders.get("/not-exist-app/dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("not-exist-app")))
                .andExpect(jsonPath("$.profiles", contains("dev")))
                .andExpect(jsonPath("$.propertySources", empty()));
    }

}
