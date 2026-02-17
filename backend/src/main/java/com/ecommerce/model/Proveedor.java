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
@Table(name = "proveedores")
public class Proveedor extends Usuario {

    @Column(nullable = false)
    private String empresa;

    public Proveedor(String nombre, String email, String password, LocalDate fechaNacimiento, String empresa) {
        super(nombre, email, password, fechaNacimiento, Role.SUPPLIER);
        this.empresa = empresa;
    }
}
