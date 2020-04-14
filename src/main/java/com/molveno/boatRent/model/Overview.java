package com.molveno.boatRent.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Overview {
    @Id
    @GeneratedValue
    private Long id;
    private  Integer numberOfTripsEnded;
    private  Integer numberOfTripsOngoing;
    private Double averageDuration;
    private Integer numberOfUsedBoats;
    private Double totalIncome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfTripsEnded() {
        return numberOfTripsEnded;
    }

    public void setNumberOfTripsEnded(Integer numberOfTripsEnded) {
        this.numberOfTripsEnded = numberOfTripsEnded;
    }

    public Integer getNumberOfTripsOngoing() {
        return numberOfTripsOngoing;
    }

    public void setNumberOfTripsOngoing(Integer numberOfTripsOngoing) {
        this.numberOfTripsOngoing = numberOfTripsOngoing;
    }

    public Double getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(Double averageDuration) {
        this.averageDuration = averageDuration;
    }

    public Integer getNumberOfUsedBoats() {
        return numberOfUsedBoats;
    }

    public void setNumberOfUsedBoats(Integer numberOfUsedBoats) {
        this.numberOfUsedBoats = numberOfUsedBoats;
    }

    public Double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Double totalIncome) {
        this.totalIncome = totalIncome;
    }

}
