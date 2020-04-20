package com.molveno.boatRent.repositories;

import com.molveno.boatRent.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByBoatsTypeAndStartDate(String boatType, LocalDate startDate);
}
