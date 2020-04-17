package com.molveno.boatRent.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class BoatControllerTest {

    @InjectMocks
    private BoatController boatController;

    @Mock
    private BoatRepository boatRepository;

    private MockMvc mockMvc;

    @Before
    public void setup () {
        mockMvc = MockMvcBuilders.standaloneSetup(boatController).build();
    }

    @Test
    public  void  dummyTest () {
        assertTrue(true);
    }

    @Test
    public  void  testGet() throws Exception{
        List<Boat> result = new ArrayList<>();
        Boat boat = new Boat();
        boat.setId(1L);
        boat.setBoatNumber("11");
        boat.setMinPrice(2.00);
        boat.setActualPrice(20.00);
        boat.setNumberOfSeats(3);
        boat.setType("rowing");

        result.add(boat);

        when (boatRepository.findAll()).thenReturn (result);
        this.mockMvc.perform(get("/api/boats"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(1)))
                .andExpect(jsonPath("$.[0].boatNumber", is("11")))
                .andExpect(jsonPath("$.[0].minPrice", is(2.00)))
                .andExpect(jsonPath("$.[0].actualPrice", is(20.00)))
                .andExpect(jsonPath("$.[0].numberOfSeats", is(3)))
                .andExpect(jsonPath("$.[0].type", is("rowing")))
                .andExpect(status().isOk());
        verify(boatRepository, times(1)).findAll();
    }

    @Test
    public  void  testPost() throws Exception{
        Boat newBoat = new Boat();
        List<Trip> trips = new ArrayList<>();
        newBoat.setId(12L);
        newBoat.setBoatNumber("15");
        newBoat.setMinPrice(2.00);
        newBoat.setActualPrice(20.00);
        newBoat.setNumberOfSeats(3);
        newBoat.setType("rowing");
        newBoat.setTrips(trips);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(newBoat);

        when (boatRepository.save(Mockito.any(Boat.class))).thenReturn (newBoat);
        this.mockMvc.perform(post("/api/boats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(jsonPath("$", is("The boat has added..")))
                .andExpect(status().isOk());
        verify(boatRepository, times(1)).save(Mockito.any(Boat.class));
    }
}