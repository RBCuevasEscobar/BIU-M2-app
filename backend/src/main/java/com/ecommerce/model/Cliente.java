package com.ecommerce.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    @Column(name = "direccion_envio")
    private String direccionEnvio;

    public Cliente(String nombre, String email, String password, LocalDate fechaNacimiento, String direccionEnvio) {
        super(nombre, email, password, fechaNacimiento, Role.CUSTOMER);
        this.direccionEnvio = direccionEnvio;
    }
}
