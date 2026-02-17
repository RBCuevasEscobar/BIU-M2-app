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
@Table(name = "productos_fisicos")
public class ProductoFisico extends Producto {

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Double peso;

    public ProductoFisico(String nombre, Double precio, Integer stock, Double peso) {
        super(nombre, precio);
        this.stock = stock;
        this.peso = peso;
    }

    @Override
    public String mostrarDetalle() {
        return "Producto Físico: " + getNombre() + ", Stock: " + stock + ", Peso: " + peso + "kg";
    }
}
