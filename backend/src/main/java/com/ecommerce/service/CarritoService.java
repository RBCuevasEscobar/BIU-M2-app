package com.ecommerce.service;

import com.ecommerce.model.Carrito;
import com.ecommerce.model.Producto;
import com.ecommerce.model.Usuario;
import com.ecommerce.repository.CarritoRepository;
import com.ecommerce.repository.ProductoRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioActual() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public Carrito obtenerCarritoActual() {
        Usuario usuario = getUsuarioActual();
        return carritoRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    Carrito carrito = new Carrito();
                    carrito.setUsuario(usuario);
                    return carritoRepository.save(carrito);
                });
    }

    @SuppressWarnings("null")
    @Transactional
    public Carrito agregarProducto(Long productoId) {
        Carrito carrito = obtenerCarritoActual();
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        carrito.agregarProducto(producto);
        return carritoRepository.save(carrito);
    }

    @SuppressWarnings("null")
    @Transactional
    public Carrito eliminarProducto(Long productoId) {
        Carrito carrito = obtenerCarritoActual();
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        carrito.eliminarProducto(producto);
        return carritoRepository.save(carrito);
    }
}
