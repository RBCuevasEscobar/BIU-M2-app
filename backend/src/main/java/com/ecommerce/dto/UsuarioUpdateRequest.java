// src/main/java/com/ecommerce/dto/UsuarioUpdateRequest.java
package com.ecommerce.dto;

import java.time.LocalDate;

import com.ecommerce.model.Role;

public record UsuarioUpdateRequest(
        String nombre,
        String email,
        String password,
        LocalDate fechaNacimiento,
        Role role,
        String rfcCurp,
        String empresa,
        LocalDate validUntil) {
}