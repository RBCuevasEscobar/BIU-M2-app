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
@Table(name = "clientes")
public class Cliente extends Usuario {

    @Column(name = "rfc_curp", length = 18, nullable = false)
    private String rfcCurp;

    public String getRfcCurp() {
        return rfcCurp;
    }

    public void setRfcCurp(String rfcCurp) {
        this.rfcCurp = rfcCurp;
    }

    public Cliente(String nombre, String email, String password, LocalDate fechaNacimiento, String rfcCurp) {
        super(nombre, email, password, fechaNacimiento, Role.CUSTOMER);
        this.rfcCurp = rfcCurp;
    }
}
