package com.ecommerce.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "administradores")
public class Administrador extends Usuario {

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    public Administrador(String nombre, String email, String password, LocalDate fechaNacimiento,
            LocalDate validUntil) {
        super(nombre, email, password, fechaNacimiento, Role.ADMIN);
        this.validUntil = validUntil;
    }
}
