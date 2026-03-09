package com.ecommerce.dto;

import lombok.Data;

@Data
public class OrdenDetalleDTO {

    private Long productoId;
    private String productoNombre;
    private Double subtotal;
    private Integer cantidad;
}