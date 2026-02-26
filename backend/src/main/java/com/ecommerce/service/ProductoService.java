package com.ecommerce.service;

import com.ecommerce.dto.ProductUpdateRequest;
import com.ecommerce.model.Producto;
import com.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @SuppressWarnings("null")
    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Producto> obtenerProducto(Long id) {
        return productoRepository.findById(id);
    }

    @SuppressWarnings("null")
    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @SuppressWarnings("null")
    @Transactional
    public Producto actualizarProducto(Long id, ProductUpdateRequest productoDetalles) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        producto.setNombre(productoDetalles.nombre());
        producto.setPrecio(productoDetalles.precio());
        // Add other fields as necessary, or use a mapper

        return productoRepository.save(producto);
    }
}
