package org.example.reservationservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.reservationservice.models.constants.ReservationStatus;
import org.example.reservationservice.models.dto.requests.CreateReservationDetailRequest;
import org.example.reservationservice.models.dto.requests.CreateReservationRequest;
import org.example.reservationservice.models.dto.responses.ReservationDetailResponse;
import org.example.reservationservice.models.dto.responses.ReservationResponse;
import org.example.reservationservice.models.dto.responses.RoomResponse;
import org.example.reservationservice.models.entities.Reservation;
import org.example.reservationservice.models.entities.ReservationDetail;
import org.example.reservationservice.models.repositories.ReservationDetailRepository;
import org.example.reservationservice.models.repositories.ReservationRepository;
import org.example.reservationservice.models.services.ReservationService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.example.reservationservice.exceptions.RoomServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

        private final ReservationRepository reservationRepository;
        private final ReservationDetailRepository reservationDetailRepository;
        private final RoomGatewayService roomGatewayService;
        private final KafkaTemplate<String, Object> kafkaTemplate;

        @Value("${spring.kafka.topic.reservation-created:reservation-created}")
        private String reservationCreatedTopic;

        @Override
        @Transactional(rollbackFor = Exception.class)
        public ReservationResponse createReservation(CreateReservationRequest request) {
                if (request == null || request.items() == null || request.items().isEmpty()) {
                        throw new IllegalArgumentException("Reservation request items cannot be empty");
                }

                record ItemData(CreateReservationDetailRequest item, RoomResponse room, double lineTotal) {}

                List<ItemData> processedItems = new ArrayList<>();
                double totalAmount = 0.0;

                for (CreateReservationDetailRequest item : request.items()) {
                        RoomResponse room = roomGatewayService.getRoomById(item.roomId());
                        if (room == null || room.pricePerNight() == null) {
                                throw new RoomServiceException("Invalid room information received for roomId: " + item.roomId());
                        }

                        double lineTotal = room.pricePerNight() * item.nights();
                        totalAmount += lineTotal;
                        processedItems.add(new ItemData(item, room, lineTotal));
                }

                Reservation reservation = Reservation.builder()
                                .customerName(request.customerName())
                                .customerEmail(request.customerEmail())
                                .totalAmount(totalAmount)
                                .status(ReservationStatus.PENDING)
                                .build();

                Reservation savedReservation = reservationRepository.save(reservation);

                List<ReservationDetail> detailEntities = new ArrayList<>();
                for (ItemData itemData : processedItems) {
                        ReservationDetail detail = ReservationDetail.builder()
                                        .reservation(savedReservation)
                                        .roomId(itemData.room().roomId() != null ? itemData.room().roomId() : itemData.item().roomId())
                                        .nights(itemData.item().nights())
                                        .pricePerNight(itemData.room().pricePerNight())
                                        .build();
                        detailEntities.add(detail);
                }

                List<ReservationDetail> savedDetails = reservationDetailRepository.saveAll(detailEntities);

                kafkaTemplate.send(reservationCreatedTopic, savedReservation.getCustomerEmail());
                log.info("Published reservation-created event to topic {} for email: {}", reservationCreatedTopic, savedReservation.getCustomerEmail());

                List<ReservationDetailResponse> detailResponses = new ArrayList<>();
                for (int i = 0; i < savedDetails.size(); i++) {
                        ReservationDetail detail = savedDetails.get(i);
                        ItemData itemData = processedItems.get(i);
                        detailResponses.add(new ReservationDetailResponse(
                                        detail.getId(),
                                        detail.getRoomId(),
                                        itemData.room().roomNumber(),
                                        itemData.room().roomType(),
                                        detail.getNights(),
                                        detail.getPricePerNight(),
                                        itemData.lineTotal()
                        ));
                }

                return new ReservationResponse(
                                savedReservation.getId(),
                                savedReservation.getCustomerName(),
                                savedReservation.getCustomerEmail(),
                                savedReservation.getTotalAmount(),
                                savedReservation.getStatus(),
                                detailResponses
                );
        }
}
