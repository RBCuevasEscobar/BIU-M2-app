package com.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdenDTO {

    private Long id;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaModificacion;

    private Long usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private String usuarioRfcCurp;

    private Double total;

    private List<OrdenDetalleDTO> detalles;

    private PaymentTransactionDTO paymentTransaction;
    private ShipmentDTO shipment;

    private DireccionDTO direccionEnvio;
}