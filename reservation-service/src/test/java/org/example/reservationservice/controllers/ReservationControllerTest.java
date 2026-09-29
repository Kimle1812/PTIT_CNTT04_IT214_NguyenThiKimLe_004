package org.example.reservationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.reservationservice.exceptions.GlobalExceptionHandler;
import org.example.reservationservice.exceptions.RoomNotFoundException;
import org.example.reservationservice.exceptions.RoomServiceException;
import org.example.reservationservice.models.constants.ReservationStatus;
import org.example.reservationservice.models.dto.requests.CreateReservationDetailRequest;
import org.example.reservationservice.models.dto.requests.CreateReservationRequest;
import org.example.reservationservice.models.dto.responses.ReservationDetailResponse;
import org.example.reservationservice.models.dto.responses.ReservationResponse;
import org.example.reservationservice.models.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReservation_Returns201Created_WhenValid() throws Exception {
        CreateReservationDetailRequest item = new CreateReservationDetailRequest(1L, 2);
        CreateReservationRequest request = new CreateReservationRequest(
                "Nguyen Van A",
                "customer@example.com",
                List.of(item)
        );

        ReservationDetailResponse detailResponse = new ReservationDetailResponse(
                1L, 1L, "101", "DELUXE", 2, 100.0, 200.0
        );
        ReservationResponse response = new ReservationResponse(
                1L, "Nguyen Van A", "customer@example.com", 200.0, ReservationStatus.PENDING, List.of(detailResponse)
        );

        when(reservationService.createReservation(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.customerEmail").value("customer@example.com"))
                .andExpect(jsonPath("$.totalAmount").value(200.0))
                .andExpect(jsonPath("$.items[0].roomNumber").value("101"))
                .andExpect(jsonPath("$.items[0].lineTotal").value(200.0));
    }

    @Test
    void createReservation_Returns400BadRequest_WhenRequestInvalid() throws Exception {
        // Customer name blank, email invalid, items empty
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                "",
                "invalid-email",
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createReservation_Returns404NotFound_WhenRoomNotFound() throws Exception {
        CreateReservationDetailRequest item = new CreateReservationDetailRequest(999L, 2);
        CreateReservationRequest request = new CreateReservationRequest(
                "Nguyen Van A",
                "customer@example.com",
                List.of(item)
        );

        when(reservationService.createReservation(any())).thenThrow(new RoomNotFoundException(999L));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Room not found"));
    }

    @Test
    void createReservation_Returns502BadGateway_WhenRoomServiceError() throws Exception {
        CreateReservationDetailRequest item = new CreateReservationDetailRequest(1L, 2);
        CreateReservationRequest request = new CreateReservationRequest(
                "Nguyen Van A",
                "customer@example.com",
                List.of(item)
        );

        when(reservationService.createReservation(any())).thenThrow(new RoomServiceException("Room service down"));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Room service error"));
    }
}
