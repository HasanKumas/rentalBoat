package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.BoatOverview;
import com.molveno.boatRent.model.Overview;
import com.molveno.boatRent.model.Trip;
import com.molveno.boatRent.repositories.BoatRepository;
import com.molveno.boatRent.repositories.BoatOverviewRepository;
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
    private BoatOverviewRepository boatOverviewRepository;
    @Autowired//connect to database
    private TripRepository tripRepository;
    @Autowired//connect to database
    private BoatRepository boatRepository;

    @GetMapping("/boatOverviews")
    public List<BoatOverview> getBoatUsageOverview () {
        List<BoatOverview> boatOverviews = new ArrayList<>();
        //to create one overview empty the repository first
        boatOverviewRepository.deleteAll();
        List<Boat> boats = boatRepository.findAll();

        //set a copy of boats to boatOverview with income and totalTime attributes added
        for (int i = 0; i < boats.size(); i++) {
            boatOverviews.add(new BoatOverview());
        }
        for(int i = 0; i < boatOverviews.size(); i++){
                boatOverviews.get(i).setBoatNumber(boats.get(i).getBoatNumber());
                boatOverviews.get(i).setNumberOfSeats(boats.get(i).getNumberOfSeats());
                Double income = 0.00;
                Integer duration = 0;
                boatOverviews.get(i).setIncome(income);
                boatOverviews.get(i).setTotalTime(duration);
                boatOverviewRepository.save(boatOverviews.get(i));
        }
        //set the income and totalTime for current day
        LocalDate today = LocalDate.now();
        List<Trip> endedTrips = tripRepository.findAllByStartDateAndStatus(today, "ended");

        for (BoatOverview boatOverview : boatOverviewRepository.findAll()){
            Double income = 0.00;
            Integer duration = 0;
            for(Trip trip : endedTrips) {
                if(trip.getBoat().getBoatNumber().equals(boatOverview.getBoatNumber())) {
                    income += trip.getTotalPrice();
                    duration += trip.getDuration();
                    boatOverview.setIncome(income);
                    boatOverview.setTotalTime(duration);
                    boatOverviewRepository.save(boatOverview);
                }
            }
        }
        return boatOverviewRepository.findAll();
    }

    @GetMapping
    public Overview getTripsOverview (){
        //no need to create a repository. An Overview instance is enough to return required information
        Overview overview = new Overview();
        //get the trips for current day according to the status. And collect the required information for overview
        LocalDate today = LocalDate.now();
        List<Trip> ongoingTrips = tripRepository.findAllByStartDateAndStatus(today, "ongoing..");
        List<Trip> endedTrips = tripRepository.findAllByStartDateAndStatus(today, "ended");
        Integer numberOfOngoingTrips = ongoingTrips.size();
        Integer numberOfEndedTrips = endedTrips.size();

        Double totalDuration = 0.00;
        Double totalPrice = 0.00;

        for(Trip trip : endedTrips){
            totalDuration += trip.getDuration();
            totalPrice += trip.getTotalPrice();
        }

        overview.setNumberOfTripsOngoing(numberOfOngoingTrips);
        overview.setNumberOfTripsEnded(numberOfEndedTrips);
        //to avoid (x/0) exception
        if(numberOfEndedTrips > 0) {
            overview.setAverageDuration(totalDuration/numberOfEndedTrips);
        }else {
            overview.setAverageDuration(0.00);
        }
        overview.setTotalIncome(totalPrice);
        overview.setNumberOfUsedBoats(numberOfEndedTrips+numberOfOngoingTrips);

        return overview;
    }
}
