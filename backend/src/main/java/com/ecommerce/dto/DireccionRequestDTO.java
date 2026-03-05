package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DireccionRequestDTO {

    private Boolean preferida;

    @Size(max = 20)
    private String alias;

    @NotBlank
    @Size(max = 25)
    private String calle;

    @NotBlank
    @Size(max = 10)
    private String numeroExterior;

    @Size(max = 10)
    private String numeroInterior;

    @Size(max = 75)
    private String referencia;

    @NotBlank
    @Size(max = 40)
    private String colonia;

    @Size(max = 45)
    private String ciudad;

    @NotBlank
    @Size(max = 45)
    private String municipio;

    @NotBlank
    @Size(max = 25)
    private String estado;

    @NotBlank
    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener exactamente 5 dígitos")
    private String codigoPostal;

    @NotNull
    @Size(min = 1, message = "Debe proporcionar al menos un teléfono")
    private List<@Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener exactamente 10 dígitos") String> telefonos;
}
