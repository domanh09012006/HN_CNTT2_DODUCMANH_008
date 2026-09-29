package org.example.doctorservice.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(name = "specialization", nullable = false)
    private String specialization;

    @Column(name = "consultation_fee", nullable = false)
    private Double consultationFee;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;
}
