package org.example.roomservice.models.repositories;

import org.example.roomservice.models.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
