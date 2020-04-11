package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("api/trips")//end point
public class TripController {
    @Autowired//connect to database
    private TripRepository tripRepository;
    @Autowired//connect to database
    private BoatRepository boatRepository;

    @GetMapping
    public List<Trip> getTrips (){
        return tripRepository.findAll();
    }

    @DeleteMapping("/{id}")
    void deleteTrip(@PathVariable Long id) {
        tripRepository.deleteById(id);
    }

    @PostMapping
    public String startTrip(@RequestParam("suitableBoatNumber") String suitableBoatNumber, @RequestParam("numOfPersons") Integer numOfPersons) {
        Trip trip = new Trip();
        //suitable boat added to trip
        Boat suitableBoat = boatRepository.findOneByBoatNumberIgnoreCase(suitableBoatNumber);
        trip.setBoat(suitableBoat);
        LocalDate today = LocalDate.now();
//        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        today.format(f);
        trip.setStartDate(today);
        LocalTime startTime = LocalTime.now();
        trip.setStartTime(startTime);
        trip.setNumberOfPersons(numOfPersons);
        trip.setStatus("ongoing..");

//        long minutes = startTime.until( endTime, ChronoUnit.MINUTES );

        tripRepository.save(trip);
        return "The trip has started..";
    }
}
