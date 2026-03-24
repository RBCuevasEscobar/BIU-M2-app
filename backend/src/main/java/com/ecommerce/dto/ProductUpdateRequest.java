package com.ecommerce.dto;

public record ProductUpdateRequest(
                String nombre,
                String descripcion,
                Double precio,
                Integer stock,
                String imagen) {
}
