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

    private String usuarioEmail;

    private Double total;

    private List<OrdenDetalleDTO> detalles;

    private PaymentTransactionDTO paymentTransaction;
    private ShipmentDTO shipment;

    private DireccionDTO direccionEnvio;
}