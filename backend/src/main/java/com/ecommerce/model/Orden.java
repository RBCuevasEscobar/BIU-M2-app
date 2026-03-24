package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado = EstadoOrden.CREATED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime ultimaModificacion;

    @Column(nullable = false)
    private Double total;

    /**
     * Subtotal puro de productos (precio × cantidad, sin IVA).
     * Se establece al crear la orden y no cambia.
     */
    @Column(name = "subtotal_productos")
    private Double subtotalProductos;

    /**
     * Tasa de IVA vigente al momento de creación de la orden.
     * Se actualiza al momento del pago si la tasa cambió.
     * Después de PAID, ya no se modifica nunca más.
     */
    @Column(name = "iva_tasa")
    private Double ivaTasa;

    /**
     * RFC/CURP del cliente al momento del pago exitoso (PAID).
     * Se captura una sola vez y no se modifica.
     */
    @Column(name = "rfc_cliente", length = 18)
    private String rfcCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDetalle> detalles = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_transaction_id", referencedColumnName = "id")
    private PaymentTransaction paymentTransaction;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_envio_id")
    private Direccion direccionEnvio;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ultimaModificacion = LocalDateTime.now();
    }

    public void agregarDetalle(OrdenDetalle detalle) {
        detalles.add(detalle);
        detalle.setOrden(this);
    }
}
