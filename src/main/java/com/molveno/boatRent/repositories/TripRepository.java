package com.molveno.boatRent.repositories;

import com.molveno.boatRent.model.Boat;
import com.molveno.boatRent.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByStatus(String status);
    List<Trip> findAllByEndDateAndStatus(LocalDate startDate, String status);
    List<Trip> findAllByBoatsTypeAndStatus(String type, String status);
    List<Trip> findAllByEndDateAndBoatsTypeAndStatus(LocalDate startDate, String type, String status);
}
