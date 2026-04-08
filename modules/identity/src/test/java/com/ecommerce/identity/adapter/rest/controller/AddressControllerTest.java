package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.address.AddressResponse;
import com.ecommerce.identity.adapter.rest.mapper.address.AddressDtoMapper;
import com.ecommerce.identity.application.dto.address.command.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.command.DeleteAddressCommand;
import com.ecommerce.identity.application.dto.address.command.SetDefaultAddressCommand;
import com.ecommerce.identity.application.dto.address.command.UpdateAddressCommand;
import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.application.port.in.address.CreateAddressInputPort;
import com.ecommerce.identity.application.port.in.address.DeleteAddressInputPort;
import com.ecommerce.identity.application.port.in.address.ListAddressesQueryPort;
import com.ecommerce.identity.application.port.in.address.SetDefaultAddressInputPort;
import com.ecommerce.identity.application.port.in.address.UpdateAddressInputPort;
import com.ecommerce.identity.domain.valueobject.AddressType;
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
    private ListAddressesQueryPort listAddressesQueryPort;

    @Mock
    private CreateAddressInputPort createAddressInputPort;

    @Mock
    private UpdateAddressInputPort updateAddressInputPort;

    @Mock
    private DeleteAddressInputPort deleteAddressInputPort;

    @Mock
    private SetDefaultAddressInputPort setDefaultAddressInputPort;

    @Mock
    private AddressDtoMapper addressDtoMapper;

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

    private AddressResult result(String id, boolean isDefault) {
        return new AddressResult(
                id,
                "user-1",
                "Tester",
                "0911222333",
                "79",
                "Ho Chi Minh",
                "760",
                "Quan 1",
                "26734",
                "Ben Nghe",
                "123 Nguyen Trai",
                "123 Nguyen Trai, Ben Nghe, Quan 1, Ho Chi Minh",
                AddressType.HOME,
                isDefault,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 11, 0));
    }

    private AddressResponse response(String id, boolean isDefault) {
        return new AddressResponse(
                id,
                "Tester",
                "0911222333",
                "79",
                "Ho Chi Minh",
                "760",
                "Quan 1",
                "26734",
                "Ben Nghe",
                "123 Nguyen Trai",
                "123 Nguyen Trai, Ben Nghe, Quan 1, Ho Chi Minh",
                AddressType.HOME,
                isDefault,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 20, 11, 0));
    }

    @Test
    void listShouldReturnAddresses() throws Exception {
        var result = result("addr-1", true);
        var response = response("addr-1", true);
        Mockito.when(listAddressesQueryPort.execute("user-1")).thenReturn(List.of(result));
        Mockito.when(addressDtoMapper.toResponses(List.of(result))).thenReturn(List.of(response));

        mockMvc().perform(get("/api/v1/addresses").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"addressId\":\"addr-1\"")));
    }

    @Test
    void createShouldReturnAddress() throws Exception {
        var result = result("addr-1", true);
        var response = response("addr-1", true);
        Mockito.when(addressDtoMapper.toCreateCommand(Mockito.anyString(), Mockito.any()))
                .thenReturn(new CreateAddressCommand("user-1", "Tester", "0911222333", "79", "760", "26734", "123",
                        AddressType.HOME, true));
        Mockito.when(createAddressInputPort.execute(Mockito.any(CreateAddressCommand.class))).thenReturn(result);
        Mockito.when(addressDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc().perform(post("/api/v1/addresses")
                .principal(auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"contactName":"Tester","phone":"0911222333","provinceCode":"79","districtCode":"760","wardCode":"26734","detailAddress":"123","type":"HOME"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void updateShouldReturnAddress() throws Exception {
        var result = result("addr-1", false);
        var response = response("addr-1", false);
        Mockito.when(addressDtoMapper.toUpdateCommand(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(new UpdateAddressCommand("user-1", "addr-1", "Tester", "0911222333", "79", "760", "26734",
                        "123", AddressType.HOME));
        Mockito.when(updateAddressInputPort.execute(Mockito.any(UpdateAddressCommand.class))).thenReturn(result);
        Mockito.when(addressDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc().perform(put("/api/v1/addresses/addr-1")
                .principal(auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"contactName":"Tester","phone":"0911222333","provinceCode":"79","districtCode":"760","wardCode":"26734","detailAddress":"123","type":"HOME"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        Mockito.when(addressDtoMapper.toDeleteCommand(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new DeleteAddressCommand("user-1", "addr-1"));

        mockMvc().perform(delete("/api/v1/addresses/addr-1").principal(auth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void setDefaultShouldReturnAddress() throws Exception {
        var result = result("addr-1", true);
        var response = response("addr-1", true);
        Mockito.when(addressDtoMapper.toSetDefaultCommand(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new SetDefaultAddressCommand("user-1", "addr-1"));
        Mockito.when(setDefaultAddressInputPort.execute(Mockito.any(SetDefaultAddressCommand.class)))
                .thenReturn(result);
        Mockito.when(addressDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc().perform(patch("/api/v1/addresses/addr-1/default").principal(auth()))
                .andExpect(status().isOk());
    }
}
