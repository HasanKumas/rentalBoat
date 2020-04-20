package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.views.BoatView;
import com.molveno.boatRent.views.TripView;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/overviews")//end point
public class OverviewController {
    @Autowired//connect to database
    private TripRepository tripRepository;
    @Autowired//connect to database
    private BoatRepository boatRepository;

    @GetMapping("/boatOverviews")
    public List<BoatView> getBoatUsageOverview () {
        List<BoatView> boatViews = new ArrayList<>();
        List<Boat> boats = boatRepository.findAll();

        //set a copy of boats to boatOverview with income and totalTime attributes added
        for (int i = 0; i < boats.size(); i++) {
            boatViews.add(new BoatView());
        }
        for(int i = 0; i < boatViews.size(); i++){
                boatViews.get(i).setBoatNumber(boats.get(i).getBoatNumber());
                boatViews.get(i).setType(boats.get(i).getType());
                boatViews.get(i).setNumberOfSeats(boats.get(i).getNumberOfSeats());
                Double income = 0.00;
                Integer duration = 0;
                boatViews.get(i).setIncome(income);
                boatViews.get(i).setTotalTime(duration);
        }
        //find all ended trips for current day
        LocalDate today = LocalDate.now();
        List<Trip> endedTrips = tripRepository.findAllByEndDateAndStatus(today, "ended");

        //set the income and totalTime for current day per boatView
        for (BoatView boatView : boatViews){
            double income = 0.00;
            double pricePerBoat;
            Integer duration = 0;
            Integer durationPerBoat;
            for(Trip trip : endedTrips) {
                pricePerBoat = trip.getTotalPrice()/trip.getBoats().size();
                durationPerBoat = (trip.getDuration());
                for(Boat boat : trip.getBoats()) {
                    if (boat.getBoatNumber().equals(boatView.getBoatNumber())) {
                        income += pricePerBoat;
                        duration += durationPerBoat;
                        boatView.setIncome(income);
                        boatView.setTotalTime(duration);
                        break;
                    }
                }
            }
        }
        return boatViews;
    }

    @GetMapping("/tripOverviews")
    public TripView getTripsOverview (){
        TripView tripView = new TripView();

        //get the trips for current day according to the status. And collect the required information for overview
        LocalDate today = LocalDate.now();
        List<Trip> ongoingTrips = tripRepository.findAllByStatus("ongoing..");
        List<Trip> endedTrips = tripRepository.findAllByEndDateAndStatus(today, "ended");
        Integer numberOfOngoingTrips = ongoingTrips.size();
        Integer numberOfEndedTrips = endedTrips.size();

        tripView.setNumberOfTripsOngoing(numberOfOngoingTrips);
        tripView.setNumberOfTripsEnded(numberOfEndedTrips);

        Double totalDuration = 0.00;
        Double totalPrice = 0.00;
        Integer numOfUsedBoats = 0;

        for(Trip trip : endedTrips){
            totalDuration += trip.getDuration();
            totalPrice += trip.getTotalPrice();
            numOfUsedBoats += trip.getBoats().size();
        }
        tripView.setTotalIncome(totalPrice);
        tripView.setNumberOfUsedBoats(numOfUsedBoats);

        //to avoid (x/0) exception
        if(numberOfEndedTrips > 0) {
            tripView.setAverageDuration(totalDuration/numberOfEndedTrips);
        }else {
            tripView.setAverageDuration(0.00);
        }

        return tripView;
    }
}
