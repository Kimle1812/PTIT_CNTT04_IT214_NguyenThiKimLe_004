package org.example.reservationservice.models.services.impl;

import org.example.reservationservice.exceptions.RoomNotFoundException;
import org.example.reservationservice.exceptions.RoomServiceException;
import org.example.reservationservice.models.constants.ReservationStatus;
import org.example.reservationservice.models.dto.requests.CreateReservationDetailRequest;
import org.example.reservationservice.models.dto.requests.CreateReservationRequest;
import org.example.reservationservice.models.dto.responses.ReservationResponse;
import org.example.reservationservice.models.dto.responses.RoomResponse;
import org.example.reservationservice.models.entities.Reservation;
import org.example.reservationservice.models.entities.ReservationDetail;
import org.example.reservationservice.models.repositories.ReservationDetailRepository;
import org.example.reservationservice.models.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationDetailRepository reservationDetailRepository;

    @Mock
    private RoomGatewayService roomGatewayService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                reservationRepository,
                reservationDetailRepository,
                roomGatewayService,
                kafkaTemplate
        );
        ReflectionTestUtils.setField(reservationService, "reservationCreatedTopic", "reservation-created");
    }

    @Test
    void createReservation_Success() {
        CreateReservationDetailRequest item1 = new CreateReservationDetailRequest(10L, 2);
        CreateReservationDetailRequest item2 = new CreateReservationDetailRequest(20L, 3);
        CreateReservationRequest request = new CreateReservationRequest(
                "Nguyen Van A",
                "vana@example.com",
                List.of(item1, item2)
        );

        RoomResponse room1 = new RoomResponse(10L, "101", "DELUXE", 100.0);
        RoomResponse room2 = new RoomResponse(20L, "202", "STANDARD", 150.0);

        when(roomGatewayService.getRoomById(10L)).thenReturn(room1);
        when(roomGatewayService.getRoomById(20L)).thenReturn(room2);

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        when(reservationDetailRepository.saveAll(any())).thenAnswer(invocation -> {
            List<ReservationDetail> list = invocation.getArgument(0);
            long id = 1;
            for (ReservationDetail d : list) {
                d.setId(id++);
            }
            return list;
        });

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Nguyen Van A", response.customerName());
        assertEquals("vana@example.com", response.customerEmail());
        assertEquals(ReservationStatus.PENDING, response.status());
        assertEquals(650.0, response.totalAmount()); // 100 * 2 + 150 * 3 = 200 + 450 = 650
        assertEquals(2, response.items().size());

        assertEquals("101", response.items().get(0).roomNumber());
        assertEquals("DELUXE", response.items().get(0).roomType());
        assertEquals(2, response.items().get(0).nights());
        assertEquals(100.0, response.items().get(0).pricePerNight());
        assertEquals(200.0, response.items().get(0).lineTotal());

        assertEquals("202", response.items().get(1).roomNumber());
        assertEquals("STANDARD", response.items().get(1).roomType());
        assertEquals(3, response.items().get(1).nights());
        assertEquals(150.0, response.items().get(1).pricePerNight());
        assertEquals(450.0, response.items().get(1).lineTotal());

        // Verify Kafka published
        verify(kafkaTemplate).send("reservation-created", "vana@example.com");

        // Verify DB saved
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationDetailRepository).saveAll(any());
    }

    @Test
    void createReservation_ThrowsRoomNotFoundException_AndDoesNotSave() {
        CreateReservationDetailRequest item = new CreateReservationDetailRequest(999L, 2);
        CreateReservationRequest request = new CreateReservationRequest(
                "Nguyen Van B",
                "vanb@example.com",
                List.of(item)
        );

        when(roomGatewayService.getRoomById(999L)).thenThrow(new RoomNotFoundException(999L));

        assertThrows(RoomNotFoundException.class, () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
        verify(reservationDetailRepository, never()).saveAll(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void createReservation_ThrowsRoomServiceException_WhenRoomServiceUnavailable() {
        CreateReservationDetailRequest item = new CreateReservationDetailRequest(10L, 2);
        CreateReservationRequest request = new CreateReservationRequest(
                "Nguyen Van C",
                "vanc@example.com",
                List.of(item)
        );

        when(roomGatewayService.getRoomById(10L)).thenThrow(new RoomServiceException("Room service is unavailable"));

        assertThrows(RoomServiceException.class, () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
        verify(reservationDetailRepository, never()).saveAll(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }
}
