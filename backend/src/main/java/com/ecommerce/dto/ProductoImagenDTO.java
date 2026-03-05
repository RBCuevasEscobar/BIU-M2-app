package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImagenDTO {
    private Long id;
    private String imagenUrl;
    private Boolean isDefault;
}
