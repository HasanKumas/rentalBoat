package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/boats")//end point
public class BoatController {
    @Autowired//connect to database
    private BoatRepository boatRepository;
    @Autowired
    private TripRepository tripRepository;

    //returns all records in repository
    @GetMapping
    public List<Boat> getBoats (){
        return boatRepository.findAll();
    }

    /**
     * Save a new boat to data base and returns confirmation.
     * If the requested body exists do not save it and return a message.
     * @param boat is the requested body
     * @return String message
     */
    @PostMapping
    public String addBoat(@RequestBody Boat boat) {
        Boat existingBoat = boatRepository.findOneByBoatNumberIgnoreCase(boat.getBoatNumber());
        if(existingBoat != null) {
            return "The boat number "+ boat.getBoatNumber() + " is already exists. Please set another number.";
        }
        boatRepository.save(boat);
        return "The boat has added..";
    }

    /**
     * Deletes a record according to given id param.
     * @param id
     */
    @DeleteMapping("/{id}")
    public void deleteBoat(@PathVariable Long id) {
        boatRepository.deleteById(id);
    }

    /**
     * updates an existing record as requested body.
     * @param id id number for the existing record.
     * @param boat is the requested body.
     */
    @PutMapping("/{id}")
    public void updateBoat(@PathVariable("id") Long id,  @RequestBody Boat boat){
        boat.setId(id);
        boatRepository.save(boat);
    }

    /**
     * Set the actual price for all the same type of boats.
     * @param boatType requested boat type
     * @param actualPrice new actual price to be set
     */
    @PutMapping("/setActualPrice")
    public void setActualPrice(@RequestParam("boatType") String boatType, @RequestParam("actualPrice") Double actualPrice){
        List<Boat> boats = boatRepository.findAllByType(boatType);
        for (Boat boat : boats){
            boat.setActualPrice(actualPrice);
            boatRepository.save(boat);
        }
    }

    /**
     * Set the minimum price for all the same type of boats.
     * @param boatType requested boat type
     * @param minPrice new minimum price to be set
     */
    @PutMapping("/setMinPrice")
    public void setMinPrice(@RequestParam("boatType") String boatType, @RequestParam("minPrice") Double minPrice){
        List<Boat> boats = boatRepository.findAllByType(boatType);
        for (Boat boat : boats){
            boat.setMinPrice(minPrice);
            boatRepository.save(boat);
        }
    }


    /**
     * This method returns a suitable boat number
     * for starting a trip. This is done according to
     * boat type and number of persons to trip.
     * Charging time for electrical boats is taken
     * into account for suitability. Also assured that
     * the rentals to be divided evenly over boats by choosing
     * the least used boat among the candidate boats.
     * @param numOfPersons the number of persons
     * @param boatType the boat type
     * @return suitableBoatNumber
     */
    @GetMapping("/suitableBoats")
    public String getSuitableBoats(@RequestParam("numOfPersons") Integer numOfPersons, @RequestParam("boatType") String boatType) {

        String suitableBoatNumber ="There is no suitable boat...";
        LocalDate today = LocalDate.now();

        //find all trips still ongoing to check boats in use
        List<Trip> ongoingTrips = tripRepository.findAllByBoatTypeAndStatus(boatType, "ongoing..");

        /*load all boats ordered by number of seats in ascending
        to a list then remove the boats inside ongoingTrips to find boats not in use*/
        List<Boat> boatsNotInUse = new ArrayList<>(boatRepository.findAllByTypeOrderByNumberOfSeatsAsc(boatType));
        for (Trip ongoingTrip : ongoingTrips){
                boatsNotInUse.remove(ongoingTrip.getBoat());
        }

        /*for electrical boats checks the end time of ended trips
         taking into account charging time for suitability*/
        if(boatType.equals("electrical")){
            //find all trips ended today for electrical boats
            List<Trip> endedTrips = tripRepository.findAllByEndDateAndBoatTypeAndStatus(today, boatType, "ended");

            for (Trip endedTrip : endedTrips) {
                long chargingTime = endedTrip.getBoat().getChargingTime();
                long duration = endedTrip.getDuration();

                LocalDateTime endedTripAvailableDateTime = LocalDateTime.of(endedTrip.getStartDate(), endedTrip.getStartTime())
                                                                                        .plusMinutes(duration + chargingTime);

                if (endedTripAvailableDateTime.isAfter(LocalDateTime.now())) {
                    boatsNotInUse.remove(endedTrip.getBoat());
                }
            }
        }
        /* In order to evenly distribute rentals
         over boats firstly the matched number of seats
         according to number of persons searched.Checked
         if exact match or nearest match available. */
        int matchedNumberOfSeats = 0;
        for (Boat boat: boatsNotInUse) {
            if (boat.getNumberOfSeats() >= numOfPersons) {
                matchedNumberOfSeats = boat.getNumberOfSeats();
                break; /*because the boats are ordered by number of seats
                        in ascending. So the first match is the nearest*/
            }
        }
        /*Secondly if there are multiple candidate boats
         available the one which has executed minimum amount
         of trips has been chosen for evenly distribution.
         matchedNumberOfSeats matched with a boat inside boatsNotInUse
         list so it is expected at least one exact match below*/
        int minCount = 1000000000;
        for (Boat boatSuits: boatsNotInUse) {
            if (boatSuits.getNumberOfSeats() == matchedNumberOfSeats) {
                if(boatSuits.getTrips().size() < minCount) {
                    suitableBoatNumber = boatSuits.getBoatNumber();
                    minCount = boatSuits.getTrips().size();
                }
            }
        }
       return suitableBoatNumber;
    }
}
