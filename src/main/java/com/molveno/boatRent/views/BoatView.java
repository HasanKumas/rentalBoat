package com.molveno.boatRent.views;

public class BoatView {
    private Long id;
    private  String boatNumber;
    private Integer numberOfSeats;
    private Integer totalTime;
    private Double income;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBoatNumber() {
        return boatNumber;
    }

    public void setBoatNumber(String boatNumber) {
        this.boatNumber = boatNumber;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }
}
