package org.example.reservationservice.models.services;

import org.example.reservationservice.models.dto.requests.CreateReservationRequest;
import org.example.reservationservice.models.dto.responses.ReservationResponse;

public interface ReservationService {

    ReservationResponse createReservation(CreateReservationRequest request);
}
