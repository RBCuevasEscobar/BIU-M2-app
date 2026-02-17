package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.OrdenRepository;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.repository.CarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    private Usuario getUsuarioActual() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public Orden crearOrdenDesdeCarrito() {
        Usuario usuario = getUsuarioActual();
        Carrito carrito = carritoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (carrito.getProductos().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setFecha(LocalDateTime.now());
        // Simular cálculo de total (debería ser más robusto en producción)

        // Agrupar productos para contar cantidad
        Map<Producto, Long> conteoProductos = carrito.getProductos().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        double total = 0.0;

        for (Map.Entry<Producto, Long> entry : conteoProductos.entrySet()) {
            Producto producto = entry.getKey();
            Integer cantidad = entry.getValue().intValue();
            Double subtotal = producto.getPrecio() * cantidad;

            OrdenDetalle detalle = new OrdenDetalle(); // Ajuste constructor si es necesario
            detalle.setCantidad(cantidad);
            detalle.setSubtotal(subtotal);
            detalle.setProducto(producto);

            orden.agregarDetalle(detalle);
            total += subtotal;
        }

        orden.setTotal(total);

        // Limpiar carrito
        carrito.getProductos().clear();
        carritoRepository.save(carrito);

        return ordenRepository.save(orden);
    }

    @SuppressWarnings("null")
    public List<Orden> listarOrdenesUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ordenRepository.findByUsuario(usuario);
    }

    public List<Orden> listarOrdenesPropias() {
        return ordenRepository.findByUsuario(getUsuarioActual());
    }

    public List<Orden> listarTodas() {
        return ordenRepository.findAll();
    }
}
