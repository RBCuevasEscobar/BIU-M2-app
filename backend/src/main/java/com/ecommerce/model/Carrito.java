package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación con Producto (ManyToMany para simplicidad en Fase 1, permitiendo
    // duplicados para cantidad)
    @ManyToMany
    @JoinTable(name = "carrito_productos", joinColumns = @JoinColumn(name = "carrito_id"), inverseJoinColumns = @JoinColumn(name = "producto_id"))
    private List<Producto> productos = new ArrayList<>();

    public void agregarProducto(Producto producto) {
        this.productos.add(producto);
    }

    public void eliminarProducto(Producto producto) {
        this.productos.remove(producto);
    }

    public Double getTotal() {
        return productos.stream().mapToDouble(Producto::getPrecio).sum();
    }
}
