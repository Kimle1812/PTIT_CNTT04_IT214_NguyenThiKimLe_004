package org.example.reservationservice.models.services.impl;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.example.reservationservice.clients.RoomClient;
import org.example.reservationservice.exceptions.RoomNotFoundException;
import org.example.reservationservice.exceptions.RoomServiceException;
import org.example.reservationservice.models.dto.responses.RoomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomGatewayServiceTest {

    @Mock
    private RoomClient roomClient;

    private RoomGatewayService roomGatewayService;

    @BeforeEach
    void setUp() {
        roomGatewayService = new RoomGatewayService(roomClient);
    }

    @Test
    void getRoomById_Success() {
        RoomResponse mockResponse = new RoomResponse(1L, "101", "DELUXE", 100.0);
        when(roomClient.getRoomById(1L)).thenReturn(mockResponse);

        RoomResponse result = roomGatewayService.getRoomById(1L);

        assertNotNull(result);
        assertEquals(1L, result.roomId());
        assertEquals("101", result.roomNumber());
        assertEquals("DELUXE", result.roomType());
        assertEquals(100.0, result.pricePerNight());
        verify(roomClient).getRoomById(1L);
    }

    @Test
    void getRoomById_ThrowsRoomNotFoundException_WhenFeignReturns404() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/rooms/99", Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        FeignException notFound = new FeignException.NotFound("Not found", request, null, null);
        when(roomClient.getRoomById(99L)).thenThrow(notFound);

        assertThrows(RoomNotFoundException.class, () -> roomGatewayService.getRoomById(99L));
    }

    @Test
    void getRoomById_ThrowsRoomServiceException_WhenFeignFails() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/rooms/1", Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        FeignException serverError = new FeignException.InternalServerError("Server error", request, null, null);
        when(roomClient.getRoomById(1L)).thenThrow(serverError);

        assertThrows(RoomServiceException.class, () -> roomGatewayService.getRoomById(1L));
    }

    @Test
    void getRoomById_ThrowsRoomServiceException_WhenResponseDataIsInvalid() {
        when(roomClient.getRoomById(1L)).thenReturn(new RoomResponse(1L, "101", "DELUXE", null));

        assertThrows(RoomServiceException.class, () -> roomGatewayService.getRoomById(1L));
    }

    @Test
    void fallback_RethrowsRoomNotFoundException() {
        RoomNotFoundException notFound = new RoomNotFoundException(1L);

        assertThrows(RoomNotFoundException.class, () -> roomGatewayService.getRoomByIdFallback(1L, notFound));
    }

    @Test
    void fallback_ThrowsRoomNotFoundException_WhenFeignStatusIs404() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/rooms/1", Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        FeignException notFound = new FeignException.NotFound("Not found", request, null, null);

        assertThrows(RoomNotFoundException.class, () -> roomGatewayService.getRoomByIdFallback(1L, notFound));
    }

    @Test
    void fallback_ThrowsRoomServiceException_WhenServiceIsUnavailable() {
        RuntimeException serviceDown = new RuntimeException("Connection refused");

        RoomServiceException exception = assertThrows(RoomServiceException.class,
                () -> roomGatewayService.getRoomByIdFallback(1L, serviceDown));
        assertTrue(exception.getMessage().contains("Room service is unavailable"));
    }
}
