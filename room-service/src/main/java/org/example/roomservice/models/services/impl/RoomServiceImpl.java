package org.example.roomservice.models.services.impl;

import org.example.roomservice.exceptions.RoomNotFoundException;
import org.example.roomservice.models.entities.Room;
import org.example.roomservice.models.repositories.RoomRepository;
import org.example.roomservice.models.services.RoomService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Cacheable(value = "rooms", key = "#id", sync = true)
    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }
}
