package org.example.reservationservice.models.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReservationDetailRequest(
        @NotNull(message = "Room id is required")
        @Min(value = 1, message = "Room id must be greater than 0")
        Long roomId,

        @NotNull(message = "Nights is required")
        @Min(value = 1, message = "Nights must be greater than 0")
        Integer nights
) {
}
