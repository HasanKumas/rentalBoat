package com.molveno.boatRent.repositories;

import com.molveno.boatRent.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Guest findOneByNameAndPhoneNumberIgnoreCase(String name, String phoneNumber);
}
