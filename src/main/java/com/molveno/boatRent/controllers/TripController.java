package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Boat suitableBoat = boatRepository.findOneByBoatNumberIgnoreCase(suitableBoatNumber);

        //set the price of trip to max of minPrice and actualPrice of Boat
        if(suitableBoat.getMinPrice() > suitableBoat.getActualPrice()){
            trip.setPrice(suitableBoat.getMinPrice());
        }else{
            trip.setPrice(suitableBoat.getActualPrice());
        }
        trip.setTotalPrice(0.00);
        trip.setBoat(suitableBoat);
        LocalDate today = LocalDate.now();
        trip.setStartDate(today);
        LocalTime startTime = LocalTime.now();
        trip.setStartTime(startTime);
        trip.setNumberOfPersons(numOfPersons);
        trip.setStatus("ongoing..");

        tripRepository.save(trip);
        return "The trip has started..";
    }
    @PutMapping("/{id}")
    public void stopTrip(@PathVariable("id") Long id){
        Double totalPrice;
        Trip startedTrip = tripRepository.getOne(id);
        startedTrip.setStatus("ended");
        LocalDate startDate = startedTrip.getStartDate();
        LocalTime startTime = startedTrip.getStartTime();
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDate endDate = LocalDate.now();
        LocalTime endTime = LocalTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        int duration = (int)startDateTime.until( endDateTime, ChronoUnit.MINUTES );
        startedTrip.setDuration(duration);
        totalPrice = (startedTrip.getPrice()*duration)/60;
        startedTrip.setTotalPrice(totalPrice);

        tripRepository.save(startedTrip);
    }
}
