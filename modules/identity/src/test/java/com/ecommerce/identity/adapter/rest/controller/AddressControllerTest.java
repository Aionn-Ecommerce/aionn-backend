package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.application.service.IdentityAddressService;
import com.ecommerce.identity.infrastructure.persistence.entity.UserAddressEntity;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private IdentityAddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(addressController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("user-1", "N/A");
    }

    private UserAddressEntity address(String id, boolean isDefault) {
        return UserAddressEntity.builder()
                .addressId(id)
                .fullAddress("123 Nguyen Trai, HCM")
                .phone("0911222333")
                .isDefault(isDefault)
                .createdAt(LocalDateTime.of(2026, 3, 20, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 20, 11, 0))
                .build();
    }

    @Test
    void listShouldReturnAddresses() throws Exception {
        Mockito.when(addressService.list("user-1")).thenReturn(List.of(address("addr-1", true)));

        mockMvc().perform(get("/api/v1/addresses").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"addressId\":\"addr-1\"")));
    }

    @Test
    void createShouldReturnAddress() throws Exception {
        Mockito.when(addressService.create("user-1", "123 Nguyen Trai, HCM", "0911222333"))
                .thenReturn(address("addr-1", true));

        mockMvc().perform(post("/api/v1/addresses")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullAddress":"123 Nguyen Trai, HCM","phone":"0911222333"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Address created")));
    }

    @Test
    void updateShouldReturnAddress() throws Exception {
        Mockito.when(addressService.update("user-1", "addr-1", "456 Le Loi, HCM", "0988777666"))
                .thenReturn(address("addr-1", false));

        mockMvc().perform(put("/api/v1/addresses/addr-1")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullAddress":"456 Le Loi, HCM","phone":"0988777666"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Address updated")));
    }

    @Test
    void deleteShouldReturnSuccess() throws Exception {
        mockMvc().perform(delete("/api/v1/addresses/addr-1").principal(auth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void setDefaultShouldReturnAddress() throws Exception {
        Mockito.when(addressService.setDefault("user-1", "addr-1")).thenReturn(address("addr-1", true));

        mockMvc().perform(patch("/api/v1/addresses/addr-1/default").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Default address updated")));
    }
}
