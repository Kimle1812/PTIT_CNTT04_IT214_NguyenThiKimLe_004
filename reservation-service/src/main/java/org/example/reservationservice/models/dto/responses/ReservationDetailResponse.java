package org.example.reservationservice.models.dto.responses;

public record ReservationDetailResponse(
        Long id,
        Long roomId,
        String roomNumber,
        String roomType,
        Integer nights,
        Double pricePerNight,
        Double lineTotal
) {
}
