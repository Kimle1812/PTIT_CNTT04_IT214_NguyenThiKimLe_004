package org.example.roomservice.models.services;

import org.example.roomservice.models.entities.Room;

public interface RoomService {
    Room getRoomById(Long id);
}
