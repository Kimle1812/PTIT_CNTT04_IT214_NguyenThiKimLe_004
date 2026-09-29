package org.example.reservationservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.reservationservice.clients.RoomClient;
import org.example.reservationservice.exceptions.RoomNotFoundException;
import org.example.reservationservice.exceptions.RoomServiceException;
import org.example.reservationservice.models.dto.responses.RoomResponse;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomGatewayService {

    private final RoomClient roomClient;

    @CircuitBreaker(name = "roomService", fallbackMethod = "getRoomByIdFallback")
    public RoomResponse getRoomById(Long roomId) {
        RoomResponse response;
        try {
            response = roomClient.getRoomById(roomId);
        } catch (FeignException.NotFound e) {
            throw new RoomNotFoundException(roomId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new RoomNotFoundException(roomId);
            }
            throw new RoomServiceException("Error calling room service for room " + roomId + ": " + e.getMessage(), e);
        }

        if (response == null || response.roomId() == null || response.pricePerNight() == null || response.pricePerNight() < 0) {
            throw new RoomServiceException("Invalid room data received from room service for id: " + roomId);
        }
        return response;
    }

    public RoomResponse getRoomByIdFallback(Long roomId, Throwable throwable) {
        log.error("Room service fallback triggered for roomId: {}, cause: {}", roomId, throwable.getMessage());
        if (throwable instanceof RoomNotFoundException e) {
            throw e;
        }
        if (throwable instanceof FeignException fe && fe.status() == 404) {
            throw new RoomNotFoundException(roomId);
        }
        if (throwable.getCause() instanceof RoomNotFoundException e) {
            throw e;
        }
        if (throwable.getCause() instanceof FeignException fe && fe.status() == 404) {
            throw new RoomNotFoundException(roomId);
        }
        if (throwable instanceof RoomServiceException e) {
            throw e;
        }
        throw new RoomServiceException("Room service is unavailable or returned an error: " + throwable.getMessage(), throwable);
    }
}
