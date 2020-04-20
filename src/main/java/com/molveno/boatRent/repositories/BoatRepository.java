package com.molveno.boatRent.repositories;

import com.molveno.boatRent.model.Boat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface BoatRepository extends JpaRepository<Boat, Long> {
    Boat findOneByBoatNumberIgnoreCase(String boatNumber);
    List<Boat> findAllByBoatNumberInIgnoreCase(List<String> boatNumbers);
    List<Boat> findAllByType(String boatType);
    List<Boat> findAllByTypeAndBlockStatusOrderByNumberOfSeatsAsc(String boatType, String blockStatus);
}
