package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.application.service.AdminUserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(adminUserController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void updateRolesShouldReturnSuccess() throws Exception {
        Mockito.when(adminUserService.updateRoles(Mockito.eq("user-1"), Mockito.anySet()))
                .thenReturn(Set.of("BUYER", "SELLER"));

        mockMvc().perform(put("/api/v1/admin/users/user-1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roles":["buyer","seller"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Roles updated")))
                .andExpect(content().string(Matchers.containsString("SELLER")));
    }

    @Test
    void removeRolesShouldReturnSuccess() throws Exception {
        Mockito.when(adminUserService.removeRoles(Mockito.eq("user-1"), Mockito.anySet()))
                .thenReturn(Set.of("BUYER"));

                mockMvc().perform(delete("/api/v1/admin/users/user-1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roles":["seller"]}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatusShouldReturnSuccess() throws Exception {
        Mockito.when(adminUserService.updateStatus("user-1", "ACTIVE")).thenReturn("ACTIVE");

        mockMvc().perform(patch("/api/v1/admin/users/user-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"status\":\"ACTIVE\"")));
    }
}


