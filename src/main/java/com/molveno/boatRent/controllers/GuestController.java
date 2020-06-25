package com.molveno.boatRent.controllers;

import com.molveno.boatRent.model.Guest;
import com.molveno.boatRent.repositories.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/guests") // end point
public class GuestController {
    @Autowired // connect to database
    private GuestRepository guestRepository;

    @GetMapping
    public List<Guest> getGuests() {
        return guestRepository.findAll();
    }

    @PostMapping
    public Long addGuest(@RequestBody Guest guest) {
        Guest existingGuest = guestRepository.findOneByNameAndPhoneNumberIgnoreCase(guest.getName(),
                guest.getPhoneNumber());
        if (existingGuest != null) {
            return existingGuest.getId();
        }
        return guestRepository.save(guest).getId();
    }

    @DeleteMapping("/{id}")
    public void deleteGuest(@PathVariable Long id) {
        guestRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public void updateGuest(@PathVariable("id") Long id, @RequestBody Guest guest) {
        guest.setId(id);
        guestRepository.save(guest);
    }
}
