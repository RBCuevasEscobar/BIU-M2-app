package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ProductoRequestDTO {
    @NotBlank
    private String nombre;

    @NotNull
    private Double precio;

    @Size(max = 500)
    private String descripcion;

    @Size(max = 150)
    private String proveedor;

    @NotBlank
    private String tipo; // "Fisico" o "Digital"

    // Campos físicos
    private Integer stock;
    private Double peso;

    // Campos digitales
    private String urlDescarga;

    // Lista de URLs de imágenes
    @NotNull
    @Size(min = 1, message = "El producto debe tener al menos una imagen")
    private List<String> imagenesUrls;

    // Indice de la imagen default en imagenesUrls
    private Integer defaultImageIndex;
}
