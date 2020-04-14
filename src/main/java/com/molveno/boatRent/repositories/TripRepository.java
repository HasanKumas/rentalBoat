package com.molveno.boatRent.repositories;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByStartDate(LocalDate startDate);
    List<Trip> findAllByStartDateAndStatus(LocalDate startDate, String status);
}
