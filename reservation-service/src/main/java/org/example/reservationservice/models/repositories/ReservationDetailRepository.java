package org.example.reservationservice.models.repositories;

import org.example.reservationservice.models.entities.ReservationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
}
