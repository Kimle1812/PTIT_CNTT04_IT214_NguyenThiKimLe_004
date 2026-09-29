package org.example.roomservice.models.services;

import org.example.roomservice.exceptions.RoomNotFoundException;
import org.example.roomservice.models.entities.Room;
import org.example.roomservice.models.repositories.RoomRepository;
import org.example.roomservice.models.services.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceImplTest {

    private RoomRepository roomRepository;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        roomService = new RoomServiceImpl(roomRepository);
    }

    @Test
    void getRoomByIdReturnsRoomWhenItExists() {
        Room room = new Room(1L, "101", "Deluxe", "Phòng nghỉ", new BigDecimal("15000000.00"), 5);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room result = roomService.getRoomById(1L);

        assertSame(room, result);
        verify(roomRepository).findById(1L);
    }

    @Test
    void getRoomByIdThrowsExceptionWhenItDoesNotExist() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        RoomNotFoundException exception = assertThrows(
                RoomNotFoundException.class,
                () -> roomService.getRoomById(99L)
        );

        assertEquals("Không tìm thấy phòng với id: 99", exception.getMessage());
    }

    @Test
    void getRoomByIdUsesRoomsCacheWithIdAsKey() throws NoSuchMethodException {
        Cacheable cacheable = RoomServiceImpl.class
                .getMethod("getRoomById", Long.class)
                .getAnnotation(Cacheable.class);

        assertEquals("rooms", cacheable.cacheNames()[0]);
        assertEquals("#id", cacheable.key());
        assertTrue(cacheable.sync());
        assertTrue(Serializable.class.isAssignableFrom(Room.class));
    }
}
