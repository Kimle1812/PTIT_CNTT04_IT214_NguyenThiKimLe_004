package org.example.reservationservice.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.reservationservice.models.constants.ReservationStatus;

@Entity
@Table(name = "reservations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

}
