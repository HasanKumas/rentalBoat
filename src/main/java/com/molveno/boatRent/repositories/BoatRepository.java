package com.molveno.boatRent.repositories;

import com.molveno.boatRent.model.Boat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatRepository extends JpaRepository<Boat, Long> {
    Boat findOneByBoatNumberIgnoreCase(String boatNumber);
}
