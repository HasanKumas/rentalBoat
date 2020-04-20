package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Reservation;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.ReservationRepository;
import com.molveno.boatRent.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/boats")//end point
public class BoatController {
    @Autowired//connect to database
    private BoatRepository boatRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private ReservationRepository reservationRepository;

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
        return "The boat has been added..";
    }

    /**
     * Deletes a record according to given id param.
     * @param id parameter
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
    public String updateBoat(@PathVariable("id") Long id,  @RequestBody Boat boat){
        Boat existingBoat = boatRepository.findOneByBoatNumberIgnoreCase(boat.getBoatNumber());
        if(existingBoat != null) {
            return "The boat number "+ boat.getBoatNumber() + " is already exists. Please set another number.";
        }
        boat.setId(id);
        boatRepository.save(boat);
        return "The boat has been modified..";
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
    public List<String> getSuitableBoats(@RequestParam("numOfPersons") Integer numOfPersons, @RequestParam("boatType") String boatType) {

         /*load all required type of boats not blocked ordered by number of seats in ascending
        to a list then remove the boats inside ongoingTrips and reservations to find boats not in use*/
        List<Boat> boatsNotInUsed = new ArrayList<>(boatRepository.findAllByTypeAndBlockStatusOrderByNumberOfSeatsAsc(boatType, "not blocked"));

        LocalDate today = LocalDate.now();

        //find all trips still ongoing today to check boats in use
        List<Boat> boatsNotInUse = new ArrayList<>(findSuitableBoatsOfToday(boatType, boatsNotInUsed, today));

        // find the boats which are already reserved for today
        List<Reservation> existingReservations = reservationRepository.findAllByBoatsTypeAndStartDate(boatType, today);

        for (Reservation existingReservation : existingReservations) {
            if (boatType.equals("electrical")) {
                long durationExp = existingReservation.getDuration();
                //check every boat to take into account charging time
                for (Boat boat : existingReservation.getBoats()) {

                        long chargingTime = boat.getChargingTime();

                        LocalDateTime matchedReservationAvailableDateTime = LocalDateTime.of(existingReservation.getStartDate(), existingReservation.getStartTime())
                                .plusMinutes(durationExp + chargingTime + 60);

                        if (matchedReservationAvailableDateTime.isAfter(LocalDateTime.now())) {
                            boatsNotInUse.remove(boat);
                        }
                }
            }else{
                LocalDateTime existingResStartDateTime = LocalDateTime.of(existingReservation.getStartDate(), existingReservation.getStartTime());
                LocalDateTime existingResAvailableDateTime = existingResStartDateTime.plusMinutes(existingReservation.getDuration() + 60);

                if (existingResAvailableDateTime.isAfter(LocalDateTime.now())) {
                    boatsNotInUse.removeAll(existingReservation.getBoats());
                }
            }
        }
        return findSuitableBoatNumbers(boatsNotInUse, numOfPersons);
    }

    @GetMapping("/suitableBoatsforReservation")
    public List<String> getSuitableBoatsForReservation(@RequestParam("numOfPersons") Integer numOfPersons,
                                                       @RequestParam("boatType") String boatType,
                                                       @RequestParam("reservationStartDateTime")  @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm") LocalDateTime reservationStartDateTime,
                                                       @RequestParam("duration") Integer duration) {

        /*check if the reservation date time is not before current time plus 1 hour
        it is assumed that a reservation is only possible at least 1 hour before */
        if(reservationStartDateTime.isBefore(LocalDateTime.now().plusMinutes(60))){
            return null;
        }

        /*An empty list created to store all suitable boats.
         Step by step boats that are not suitable will be removed from this list.
         At the end the required amount of boats will be chosen from this list.*/
        List<Boat> boatsNotInUse = new ArrayList<>();

        /*find all trips still ongoing if the reservation date is today to check boats in use
        load all required type of boats not blocked ordered by number of seats in ascending order
        to a list then remove the boats inside ongoingTrips and reservations to find boats not in use.
        In the end fill the main boatsNotInUse list with updated suitable boats*/
        List<Boat> boatsNotInUsed = new ArrayList<>(boatRepository.findAllByTypeAndBlockStatusOrderByNumberOfSeatsAsc(boatType, "not blocked"));
        LocalDate today = LocalDate.now();

        if(reservationStartDateTime.toLocalDate().isEqual(today)) {
            boatsNotInUse.addAll(findSuitableBoatsOfToday(boatType, boatsNotInUsed, today));
        }else{
            boatsNotInUse.addAll(boatsNotInUsed);
        }

        /*Check the existing reservations to find out suitable boats.
         Firstly find the boats which are already reserved for the same day.
         Then check the availability according to time and restrictions.
         */
        List<Reservation> existingReservations = reservationRepository.findAllByBoatsTypeAndStartDate(boatType, reservationStartDateTime.toLocalDate());

        LocalDateTime reservationEndDateTime = reservationStartDateTime.plusMinutes(duration);

        for (Reservation existingReservation : existingReservations) {

            if (boatType.equals("electrical")) {
                long durationExp = existingReservation.getDuration();
                //check every boat to take into account charging time. A reservation can have multiple boats
                for (Boat boat : existingReservation.getBoats()) {

                    long chargingTime = boat.getChargingTime();

                    LocalDateTime existingReservationStartDateTime = LocalDateTime.of(existingReservation.getStartDate(), existingReservation.getStartTime());
                    LocalDateTime existingReservationAvailableEndDateTime = existingReservationStartDateTime.plusMinutes(durationExp + chargingTime + 60);

                    if (!(((reservationStartDateTime.isBefore(existingReservationStartDateTime)) && (reservationEndDateTime.plusMinutes(chargingTime + 60).isBefore(existingReservationStartDateTime))) ||
                            (reservationStartDateTime.isAfter(existingReservationAvailableEndDateTime) && reservationEndDateTime.isAfter(existingReservationAvailableEndDateTime))))
                    {
                        boatsNotInUse.removeAll(existingReservation.getBoats());
                    }
                }
            }else{
                LocalDateTime existingReservationStartDateTime = LocalDateTime.of(existingReservation.getStartDate(), existingReservation.getStartTime());
                LocalDateTime existingReservationAvailableEndDateTime = existingReservationStartDateTime.plusMinutes(existingReservation.getDuration() + 60);

                if (!(((reservationStartDateTime.isBefore(existingReservationStartDateTime)) && (reservationEndDateTime.plusMinutes(60).isBefore(existingReservationStartDateTime))) ||
                        (reservationStartDateTime.isAfter(existingReservationAvailableEndDateTime) && reservationEndDateTime.isAfter(existingReservationAvailableEndDateTime))))
                {
                    boatsNotInUse.removeAll(existingReservation.getBoats());
                }
            }
        }
        return findSuitableBoatNumbers(boatsNotInUse, numOfPersons);
    }

    /**
     * This method returns the suitable boat numbers required for a trip or reservation.
     * Chooses the ones which are used least to assure evenly distribution.
     * @param boatsNotInUse all the suitable boats
     * @param numOfPersons the number of persons
     * @return suitable boat numbers
     */
    private List<String> findSuitableBoatNumbers(List<Boat> boatsNotInUse, Integer numOfPersons) {
        /* In order to evenly distribute rentals
         over boats firstly the matched number of seats
         according to number of persons searched.Checked
         if exact match or nearest match available. */
        List<String> suitableBoatNumbers =new ArrayList<>();
        List<Integer> matchedNumberOfSeats = new ArrayList<>();
        List<Boat> suitableBoats = new ArrayList<>(boatsNotInUse);
        int matchedNumberOfSeat = 0;
        int persons = numOfPersons;
        int currentMatchedNumberOfSeat;

        Matched:
        do {
            currentMatchedNumberOfSeat = 0;
            for (Boat boat : suitableBoats) {

                if (boat.getNumberOfSeats() >= persons) {/*because the boats are ordered by number of seats
                                                            in ascending. So the first match is the nearest*/
                    matchedNumberOfSeats.add(boat.getNumberOfSeats());
                    suitableBoats.remove(boat);
                    matchedNumberOfSeat += persons;
                    currentMatchedNumberOfSeat = persons;
                    if (matchedNumberOfSeat >= numOfPersons) {
                        break Matched;
                    }else{
                        persons = numOfPersons - currentMatchedNumberOfSeat +1;
                        break ;
                    }
                }

            }
            persons--;

        } while(persons > 0 && suitableBoats.size() > 0);

        if (matchedNumberOfSeat < numOfPersons){
            return null;
        }

        /*Secondly if there are multiple candidate boats
         available the one which has executed minimum amount
         of trips has been chosen for evenly distribution.
         matchedNumberOfSeats matched with a boat inside boatsNotInUse
         list so it is expected at least one exact match below*/
        for(Integer matchedNumOfSeat : matchedNumberOfSeats) {
            int minCount = 1000000000;
            String  suitableBoatNumber = "";
            int index = 0;
            for (int i =0; i < boatsNotInUse.size(); i++) {
                if (boatsNotInUse.get(i).getNumberOfSeats().equals(matchedNumOfSeat)) {
                    if (boatsNotInUse.get(i).getTrips().size() < minCount) {
                        suitableBoatNumber = boatsNotInUse.get(i).getBoatNumber();
                        index = i;
                        minCount = boatsNotInUse.get(i).getTrips().size();
                    }
                }
            }
            suitableBoatNumbers.add(suitableBoatNumber);
            boatsNotInUse.remove(index);
        }
        return suitableBoatNumbers;
    }

    /**
     * This method returns the suitable boats that can be used for the current day
     * @param boatType boat type
     * @param boatsNotInUse a candidate list of boats to be checked
     * @param today current day
     * @return suitable boats for current day
     */
    private List<Boat> findSuitableBoatsOfToday(String boatType, List<Boat> boatsNotInUse, LocalDate today) {
        List<Trip> ongoingTrips = tripRepository.findAllByBoatsTypeAndStatus(boatType, "ongoing..");

        for (Trip ongoingTrip : ongoingTrips) {
            boatsNotInUse.removeAll(ongoingTrip.getBoats());
        }

            /*for electrical boats checks the end time of ended trips
             taking into account charging time for suitability*/
        if (boatType.equals("electrical")) {
            //find all trips ended today for electrical boats
            List<Trip> endedTrips = tripRepository.findAllByEndDateAndBoatsTypeAndStatus(today, boatType, "ended");

            for (Trip endedTrip : endedTrips) {
                long durationExp = endedTrip.getDuration();
                //check every boat to take into account charging time
                for (Boat boat : endedTrip.getBoats()) {

                    long chargingTime = boat.getChargingTime();

                    LocalDateTime endedTripAvailableDateTime = LocalDateTime.of(endedTrip.getStartDate(), endedTrip.getStartTime())
                            .plusMinutes(durationExp + chargingTime);

                    if (endedTripAvailableDateTime.isAfter(LocalDateTime.now())) {
                        boatsNotInUse.remove(boat);
                    }
                }
            }
        }
        return boatsNotInUse;
    }

    /**
     * This method blocks or unblocks a boat depending on request.
     * @param boatNumber boat number
     * @param blockStatus requested block action("blocked" or "not blocked"
     * @return returns a confirmation message
     */
    @PutMapping("/blocked")
    public String blockBoat(@RequestParam("boatNumber") String boatNumber, @RequestParam("blockStatus") String blockStatus){

        Boat boat = boatRepository.findOneByBoatNumberIgnoreCase(boatNumber);

        if(boat == null) {
            return "The boat number is not exists. Please set another number.";
        }

        if(boat.getBlockStatus().equals(blockStatus)){
            return "The boat is already " + blockStatus + ". Try another boat number! ";
        }else {
            boat.setBlockStatus(blockStatus);
            boatRepository.save(boat);
        }

        return "The boat is " + blockStatus;
    }
}
