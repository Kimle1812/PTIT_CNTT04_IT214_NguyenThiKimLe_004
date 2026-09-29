package org.example.reservationservice.models.dto.responses;

import com.fasterxml.jackson.annotation.JsonAlias;

public record RoomResponse(
        @JsonAlias("id") Long roomId,
        String roomNumber,
        String roomType,
        Double pricePerNight
) {
}
