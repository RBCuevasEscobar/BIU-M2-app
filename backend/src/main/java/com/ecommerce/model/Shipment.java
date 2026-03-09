package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateDispatch;

    @Column
    private LocalDateTime dateDelivered;

    @Column(nullable = false)
    private String courier;

    @Column(nullable = false)
    private String trackingNumber;

    @OneToOne(mappedBy = "shipment")
    @JsonIgnore
    private Orden orden;

    public Shipment(LocalDateTime dateDispatch, String courier, String trackingNumber) {
        this.dateDispatch = dateDispatch;
        this.courier = courier;
        this.trackingNumber = trackingNumber;
    }
}
