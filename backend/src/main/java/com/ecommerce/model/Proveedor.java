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
@Table(name = "proveedores")
public class Proveedor extends Usuario {

    @Column(nullable = false)
    private String empresa;

    public Proveedor(String nombre, String email, String password, LocalDate fechaNacimiento, String empresa) {
        super(nombre, email, password, fechaNacimiento, Role.SUPPLIER);
        this.empresa = empresa;
    }
}
