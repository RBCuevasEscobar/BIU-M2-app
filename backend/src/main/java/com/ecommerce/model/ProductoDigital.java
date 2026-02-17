package com.ecommerce.model;

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
@Table(name = "productos_digitales")
public class ProductoDigital extends Producto {

    @Column(name = "url_descarga", nullable = false)
    private String urlDescarga;

    public ProductoDigital(String nombre, Double precio, String urlDescarga) {
        super(nombre, precio);
        this.urlDescarga = urlDescarga;
    }

    @Override
    public String mostrarDetalle() {
        return "Producto Digital: " + getNombre() + ", URL: " + urlDescarga;
    }
}
