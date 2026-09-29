package org.example.reservationservice.models.repositories;

import org.example.reservationservice.models.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
