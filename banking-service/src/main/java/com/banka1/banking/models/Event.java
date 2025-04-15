package com.banka1.banking.models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageType;

    @Lob
    private String payload; // JSON kao string

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventDelivery> deliveries = new ArrayList<>();
}