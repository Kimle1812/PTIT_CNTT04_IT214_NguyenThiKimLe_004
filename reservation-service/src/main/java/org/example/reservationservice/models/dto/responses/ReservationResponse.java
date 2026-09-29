package org.example.reservationservice.models.dto.responses;

import org.example.reservationservice.models.constants.ReservationStatus;

import java.util.List;

public record ReservationResponse(
        Long id,
        String customerName,
        String customerEmail,
        Double totalAmount,
        ReservationStatus status,
        List<ReservationDetailResponse> items
) {
}
