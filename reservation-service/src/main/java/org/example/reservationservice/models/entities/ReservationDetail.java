package org.example.reservationservice.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservation_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReservationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "nights")
    private Integer nights;

    @Column(name = "price_per_night")
    private Double pricePerNight;
}
