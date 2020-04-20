package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Guest;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.GuestRepository;
import com.molveno.boatRent.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("api/trips")//end point
public class TripController {
    @Autowired//connect to database
    private TripRepository tripRepository;
    @Autowired
    private BoatRepository boatRepository;
    @Autowired
    private GuestRepository guestRepository;

    @GetMapping
    public List<Trip> getTrips (){
        return tripRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteTrip(@PathVariable Long id) {
        tripRepository.deleteById(id);
    }

    /**To start set the the initial values by creating a new trip
     * for the requested boat. start date and time is set to current
     * date and time in constructor. Status set to "ongoing" in constructor as well.
     * Add the requested guest and the suitable boat to the trip.
     * @param suitableBoatNumbers 1st parameter
     * @param numOfPersons 2nd parameter
     * @param guestId 3rd parameter
     * @return a confirmation message
     */
    @PostMapping
    public String startTrip(@RequestParam("suitableBoatNumber") List<String> suitableBoatNumbers, @RequestParam("numOfPersons") Integer numOfPersons, @RequestParam("guestId") Long guestId) {
        Trip trip = new Trip();
        Guest guest = guestRepository.getOne(guestId);
        List<Boat> suitableBoats = boatRepository.findAllByBoatNumberInIgnoreCase(suitableBoatNumbers);

         /*set the price of the trip to the max of minPrice
          * and actualPrice of Boat. Check all the suitable
          * boats and sum their prices to find the trip price
          * This price will be used to calculate total price
          */
         Double tripPrice = 0.00;
         for (Boat suitableBoat : suitableBoats) {
             if (suitableBoat.getMinPrice() > suitableBoat.getActualPrice()) {
                 tripPrice += suitableBoat.getMinPrice();
             } else {
                 tripPrice += suitableBoat.getActualPrice();
             }
         }
        trip.setPrice(tripPrice);

        trip.setNumberOfPersons(numOfPersons);

        //add suitable boat and guest to the trip
        trip.setBoats(suitableBoats);
        trip.setGuest(guest);

        tripRepository.save(trip);

        return "The trip has started..";
    }

    /**
     * To stop the requested trip with id number
     * updates the status and
     * calculates the duration and total price
     * @param id requested trip id
     * @return the the stopped trip object
     */
    @PutMapping("/{id}")
    public Trip stopTrip(@PathVariable("id") Long id){
        double totalPrice;

        //find the requested trip from database
        Trip startedTrip = tripRepository.getOne(id);

        //update trip satus to "ended"
        startedTrip.setStatus("ended");

        /*calculate the duration in minutes by getting the difference
          between starting datetime and current datetime
         */
        LocalDate startDate = startedTrip.getStartDate();
        LocalTime startTime = startedTrip.getStartTime();
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

        LocalDate endDate = LocalDate.now();
        LocalTime endTime = LocalTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        int duration = (int)startDateTime.until( endDateTime, ChronoUnit.MINUTES );

        startedTrip.setDuration(duration);
        startedTrip.setEndDate(endDate);

        //calculate total price according to current trip price per hour and duration
        totalPrice = (startedTrip.getPrice()*duration)/60;

        startedTrip.setTotalPrice(totalPrice);

        return tripRepository.save(startedTrip);
    }
}
