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

    /** Subtotal de productos sin IVA */
    private Double subtotalProductos;

    /** Tasa de IVA aplicada a esta orden (ej. 0.16) */
    private Double ivaTasa;

    /** Monto de impuestos = subtotalProductos × ivaTasa */
    private Double montoImpuestos;

    /** RFC del cliente capturado al momento del pago */
    private String rfcCliente;

    private List<OrdenDetalleDTO> detalles;

    private PaymentTransactionDTO paymentTransaction;
    private ShipmentDTO shipment;

    private DireccionDTO direccionEnvio;
}