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

/**
 * Servicio que gestiona las operaciones del carrito de compras.
 *
 * ═══════════════════════════════════════════════════════════════
 * CONCEPTO OOP: SOBRECARGA DE MÉTODOS (Method Overloading)
 * ═══════════════════════════════════════════════════════════════
 * A nivel de servicio, agregarProducto() está SOBRECARGADO con
 * tres firmas que cubren diferentes escenarios de uso:
 *
 * Firma 1: agregarProducto(Long productoId)
 * → Método PRINCIPAL, usado por el controlador REST.
 * Busca el producto en BD por ID (flujo normal).
 *
 * Firma 2: agregarProducto(Producto producto)
 * → Útil cuando ya se tiene el objeto Producto en
 * memoria (evita una consulta extra a BD).
 * Delega a la firma 1 usando el ID del producto.
 *
 * Firma 3: agregarProducto(String nombre, Double precio)
 * → Útil para pruebas o integración, crea un
 * ProductoFisico temporal y lo agrega.
 * Delega al método del modelo Carrito.
 *
 * REGLA DE SOBRECARGA: misma clase, mismo nombre, distinta firma.
 * La resolución ocurre en TIEMPO DE COMPILACIÓN (early binding).
 * ═══════════════════════════════════════════════════════════════
 */
@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioActual() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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

    // ─────────────────────────────────────────────────────────────
    // SOBRECARGA 1 (PRINCIPAL): Agrega producto por ID.
    // Este método es el que invoca el controlador REST existente.
    // Es el punto de entrada estable del sistema → no se modifica.
    // ─────────────────────────────────────────────────────────────
    @SuppressWarnings("null")
    @Transactional
    public Carrito agregarProducto(Long productoId) {
        Carrito carrito = obtenerCarritoActual();
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        carrito.agregarProducto(producto); // Llama a SOBRECARGA 1 del modelo
        return carritoRepository.save(carrito);
    }

    // ─────────────────────────────────────────────────────────────
    // SOBRECARGA 2: Agrega un objeto Producto ya cargado.
    // Evita consulta a BD cuando el producto ya está en memoria.
    // Delega INTERNAMENTE a la firma principal (SOBRECARGA 1).
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public Carrito agregarProducto(Producto producto) {
        // Delegación al método principal usando el ID del producto
        return agregarProducto(producto.getId());
    }

    // ─────────────────────────────────────────────────────────────
    // SOBRECARGA 3: Agrega un producto dado su nombre y precio.
    // Útil para testing o contextos especiales donde no se conoce
    // el ID. Crea un ProductoFisico genérico y lo persiste antes
    // de agregarlo al carrito.
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public Carrito agregarProducto(String nombre, Double precio) {
        Carrito carrito = obtenerCarritoActual();
        // Utiliza la SOBRECARGA 3 del modelo Carrito directamente
        carrito.agregarProducto(nombre, precio);
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

    @SuppressWarnings("null")
    @Transactional
    public Carrito vaciarCarrito() {
        Carrito carrito = obtenerCarritoActual();
        carrito.getProductos().clear();
        return carritoRepository.save(carrito);
    }

}
