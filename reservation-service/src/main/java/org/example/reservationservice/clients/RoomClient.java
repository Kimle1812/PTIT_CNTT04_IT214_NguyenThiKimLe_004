package org.example.reservationservice.clients;

import org.example.reservationservice.models.dto.responses.RoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", path = "/api/v1/rooms")
public interface RoomClient {

    @GetMapping("/{id}")
    RoomResponse getRoomById(@PathVariable Long id);
}
