package com.banka1.banking.models;

import com.banka1.banking.models.helper.DeliveryStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class EventDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; // npr. SUCCESS, FAILED, PENDING

    private int httpStatus;

    @Lob
    private String responseBody;

    private long durationMs; // trajanje u milisekundama
}
