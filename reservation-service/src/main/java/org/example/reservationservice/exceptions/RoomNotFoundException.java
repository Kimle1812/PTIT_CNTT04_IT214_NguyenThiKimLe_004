package org.example.reservationservice.exceptions;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Long roomId) {
        super("Room not found with id: " + roomId);
    }
}
