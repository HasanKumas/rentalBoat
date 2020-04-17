package com.molveno.boatRent.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molveno.boatRent.model.Guest;
import com.molveno.boatRent.repositories.GuestRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;


import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class GuestControllerTest {
    @InjectMocks
    private GuestController guestController;

    @Mock
    private GuestRepository guestRepository;

    private MockMvc mockMvc;

    @Before
    public void setup () {
        mockMvc = MockMvcBuilders.standaloneSetup(guestController).build();
    }

    @Test
    public void deleteGuest() throws Exception{

        this.mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/guests/{id}", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(guestRepository, times(1)).deleteById(1L);
    }

    @Test
    public void updateGuest() throws Exception {
        Guest newGuest = new Guest();
        newGuest.setId(1L);
        newGuest.setName("Koen");
        newGuest.setIdType("identity");
        newGuest.setIdNumber("06021547");
        newGuest.setPhoneNumber("031653476");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(newGuest);

        when (guestRepository.save(Mockito.any(Guest.class))).thenReturn (newGuest);
        this.mockMvc.perform(put("/api/guests/{id}", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isOk());
        verify(guestRepository, times(1)).save(Mockito.any(Guest.class));
    }
}