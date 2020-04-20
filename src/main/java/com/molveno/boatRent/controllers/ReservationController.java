package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Guest;
import com.molveno.boatRent.model.Reservation;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.GuestRepository;
import com.molveno.boatRent.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/reservations")//end point
public class ReservationController {
    @Autowired//connect to database
    private ReservationRepository reservationRepository;
    @Autowired
    private BoatRepository boatRepository;
    @Autowired
    private GuestRepository guestRepository;

    @GetMapping
    public List<Reservation> getReservations (){
        return reservationRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationRepository.deleteById(id);
    }


    /**
     * To make a reservation create a new reservation and add the required fields
     * Calculates total price and adds it.
     * Adds the requested guest and the suitable boats.
     * @param suitableBoatNumbers 1st parameter
     * @param numOfPersons 2nd parameter
     * @param reservationStartDateTime 3rd parameter
     * @param resDuration 4th parameter
     * @param guestId 5th parameter
     * @return 6th parameter
     */
    @PostMapping
    public String addReservation(@RequestParam("suitableBoatNumbers") List<String> suitableBoatNumbers,
                                 @RequestParam("numOfPersons") Integer numOfPersons,
                                 @RequestParam("reservationStartDateTime")  @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")  LocalDateTime reservationStartDateTime,
                                 @RequestParam("resDuration") Integer resDuration,
                                 @RequestParam("guestId") Long guestId) {

        Reservation reservation = new Reservation();
        Guest guest = guestRepository.getOne(guestId);
        List<Boat> suitableBoats = boatRepository.findAllByBoatNumberInIgnoreCase(suitableBoatNumbers);
        //set the suitable boats and guest
        reservation.setBoats(suitableBoats);
        reservation.setGuest(guest);

        reservation.setNumOfPersons(numOfPersons);

        reservation.setStartDate(reservationStartDateTime.toLocalDate());
        reservation.setStartTime(reservationStartDateTime.toLocalTime());
        reservation.setDuration(resDuration);
        reservation.setEndTime(reservationStartDateTime.plusMinutes(resDuration).toLocalTime());
        reservation.setEndDate(reservationStartDateTime.plusMinutes(resDuration).toLocalDate());

        /*set the price of the reservation to the minPrice
         * of Boat. Check all the suitable boats and sum their
         *  prices to find the reservation price per hour
         * This price will be used to calculate total price
         */
        Double resPrice = 0.00;
        for (Boat suitableBoat : suitableBoats) {

                resPrice += suitableBoat.getMinPrice();
        }

        reservation.setReservationPrice(resPrice*resDuration/60);

        reservationRepository.save(reservation);

        return "The reservation has completed..\n" +
                "Your reservation number: " + reservation.getId() + "\n" +
                "Reserved boat numbers: " + suitableBoatNumbers + "\n" +
                "Total Price for the reservation: " + reservation.getReservationPrice();
    }
}
