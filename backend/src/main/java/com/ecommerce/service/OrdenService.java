package com.ecommerce.service;

import com.ecommerce.dto.OrdenDTO;
import com.ecommerce.dto.OrdenDetalleDTO;
import com.ecommerce.dto.PaymentTransactionDTO;
import com.ecommerce.dto.ShipmentDTO;
import com.ecommerce.inventory.GestorInventario;
import com.ecommerce.inventory.GestorInventarioFactory;
import com.ecommerce.model.*;
import com.ecommerce.payment.ProcesoPago;
import com.ecommerce.payment.ProcesoPagoFactory;
import com.ecommerce.repository.CarritoRepository;
import com.ecommerce.repository.OrdenRepository;
import com.ecommerce.repository.PaymentTransactionRepository;
import com.ecommerce.repository.ShipmentRepository;
import com.ecommerce.repository.UsuarioRepository;
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

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private GestorInventarioFactory inventarioFactory;

    @Autowired
    private ProcesoPagoFactory pagoFactory;

    private Usuario getUsuarioActual() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * FASE 1: Creación de la Orden (CREATED)
     * Convierte el carrito actual en una nueva orden en estado CREATED.
     */
    @Transactional
    public OrdenDTO crearOrdenDesdeCarrito() {
        Usuario usuario = getUsuarioActual();
        Carrito carrito = carritoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (carrito.getProductos().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setEstado(EstadoOrden.CREATED);

        Map<Producto, Long> conteoProductos = carrito.getProductos().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        double total = 0.0;
        for (Map.Entry<Producto, Long> entry : conteoProductos.entrySet()) {
            Producto producto = entry.getKey();
            Integer cantidad = entry.getValue().intValue();
            Double subtotal = producto.getPrecio() * cantidad;

            OrdenDetalle detalle = new OrdenDetalle(cantidad, subtotal, producto);
            orden.agregarDetalle(detalle);
            total += subtotal;
        }

        orden.setTotal(total);

        // Limpiar carrito
        carrito.getProductos().clear();
        carritoRepository.save(carrito);

        return toDTO(ordenRepository.save(orden));
    }

    /**
     * FASE 2: Procesar Pago y Verificar Stock
     */
    @Transactional
    public OrdenDTO procesarPago(Long ordenId, String metodoPago) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        // Validaciones Máquina de Estados
        if (orden.getEstado() == EstadoOrden.PAID || orden.getEstado() == EstadoOrden.SHIPPED
                || orden.getEstado() == EstadoOrden.DELIVERED) {
            throw new RuntimeException("La orden ya fue pagada o procesada.");
        }
        if (orden.getEstado() == EstadoOrden.CANCELLED) {
            throw new RuntimeException("La orden está cancelada.");
        }

        // 1. Verificar Stock Múltiple (OOP Abstraction)
        boolean hasStock = true;
        for (OrdenDetalle detalle : orden.getDetalles()) {
            GestorInventario gestor = inventarioFactory.obtenerGestor(detalle.getProducto());
            if (!gestor.verificarStock(detalle.getProducto(), detalle.getCantidad())) {
                hasStock = false;
                break;
            }
        }

        if (!hasStock) {
            orden.setEstado(EstadoOrden.OUT_OF_STOCK);
            return toDTO(ordenRepository.save(orden));
        }

        // Transición a Payment Pending si hay stock
        orden.setEstado(EstadoOrden.PAYMENT_PENDING);

        // 2. Procesar Pago (OOP Interface)
        ProcesoPago procesoPago = pagoFactory.obtenerMetodo(metodoPago);
        procesoPago.iniciarPago(orden);

        boolean pagoExitoso = procesoPago.verificarPago(orden);

        if (pagoExitoso) {
            PaymentTransaction trx = procesoPago.confirmarPago(orden, orden.getTotal());
            if (trx != null) {
                trx = paymentTransactionRepository.save(trx);
                orden.setPaymentTransaction(trx);
            }
            orden.setEstado(EstadoOrden.PAID);

            // 3. Descontar Stock definitivo porque ya se pagó
            for (OrdenDetalle detalle : orden.getDetalles()) {
                GestorInventario gestor = inventarioFactory.obtenerGestor(detalle.getProducto());
                gestor.actualizarStock(detalle.getProducto(), -detalle.getCantidad());
            }
        } else {
            // Se queda en pending si falla (el prompt indica: "Si un intento de pago falla,
            // el estado permanece PAYMENT_PENDING")
            orden.setEstado(EstadoOrden.PAYMENT_PENDING);
        }

        return toDTO(ordenRepository.save(orden));
    }

    /**
     * Cancelar Orden (Cualquiera mientras no esté pagada/enviada)
     */
    @Transactional
    public OrdenDTO cancelarOrden(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        validarPropietarioOAdmin(orden);

        if (orden.getEstado() == EstadoOrden.PAID || orden.getEstado() == EstadoOrden.SHIPPED
                || orden.getEstado() == EstadoOrden.DELIVERED) {
            throw new RuntimeException("No se puede cancelar en estado: " + orden.getEstado());
        }

        orden.setEstado(EstadoOrden.CANCELLED);
        // Si no descontamos stock sino hasta PAID, no devolvemos nada al cancelar.
        return toDTO(ordenRepository.save(orden));
    }

    /**
     * Despachar Orden (Solo ADMIN)
     */
    @Transactional
    public OrdenDTO despacharOrden(Long ordenId, String courier, String trackingNumber) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (orden.getEstado() != EstadoOrden.PAID) {
            throw new RuntimeException("Despacho permitido solo si estado = PAID");
        }

        Shipment shipment = new Shipment(LocalDateTime.now(), courier, trackingNumber);
        shipment = shipmentRepository.save(shipment);

        orden.setShipment(shipment);
        orden.setEstado(EstadoOrden.SHIPPED);

        return toDTO(ordenRepository.save(orden));
    }

    /**
     * Confirmar Entrega (Solo ADMIN)
     */
    @Transactional
    public OrdenDTO entregarOrden(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (orden.getEstado() != EstadoOrden.SHIPPED) {
            throw new RuntimeException("La orden debe estar despachada (SHIPPED)");
        }

        if (orden.getShipment() != null) {
            orden.getShipment().setDateDelivered(LocalDateTime.now());
        }
        orden.setEstado(EstadoOrden.DELIVERED);

        return toDTO(ordenRepository.save(orden));
    }

    private void validarPropietarioOAdmin(Orden orden) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !orden.getUsuario().getEmail().equals(auth.getName())) {
            throw new RuntimeException("No tiene permisos sobre esta orden.");
        }
    }

    public List<OrdenDTO> listarOrdenesUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ordenRepository.findByUsuario(usuario).stream().map(this::toDTO).toList();
    }

    public List<OrdenDTO> listarOrdenesPropias() {
        return ordenRepository.findByUsuario(getUsuarioActual()).stream().map(this::toDTO).toList();
    }

    private OrdenDTO toDTO(Orden orden) {

        OrdenDTO dto = new OrdenDTO();

        dto.setId(orden.getId());
        dto.setEstado(orden.getEstado().name());
        dto.setFechaCreacion(orden.getFechaCreacion());
        dto.setUltimaModificacion(orden.getUltimaModificacion());

        dto.setUsuarioEmail(orden.getUsuario().getEmail());

        dto.setTotal(
                orden.getDetalles()
                        .stream()
                        .mapToDouble(d -> d.getSubtotal() * d.getCantidad())
                        .sum());

        dto.setDetalles(
                orden.getDetalles().stream().map(d -> {
                    OrdenDetalleDTO det = new OrdenDetalleDTO();
                    det.setProductoId(d.getProducto().getId());
                    det.setProductoNombre(d.getProducto().getNombre());
                    det.setSubtotal(d.getSubtotal());
                    det.setCantidad(d.getCantidad());
                    return det;
                }).toList());

        if (orden.getPaymentTransaction() != null) {
            PaymentTransactionDTO pDto = new PaymentTransactionDTO();
            pDto.setId(orden.getPaymentTransaction().getId());
            pDto.setPaymentMethod(orden.getPaymentTransaction().getPaymentMethod());
            pDto.setStatus(orden.getPaymentTransaction().getStatus());
            pDto.setAmount(orden.getPaymentTransaction().getAmount());
            pDto.setTransactionDate(orden.getPaymentTransaction().getTransactionDate());
            dto.setPaymentTransaction(pDto);
        }

        if (orden.getShipment() != null) {
            ShipmentDTO sDto = new ShipmentDTO();
            sDto.setId(orden.getShipment().getId());
            sDto.setDateDispatch(orden.getShipment().getDateDispatch());
            sDto.setDateDelivered(orden.getShipment().getDateDelivered());
            sDto.setCourier(orden.getShipment().getCourier());
            sDto.setTrackingNumber(orden.getShipment().getTrackingNumber());
            dto.setShipment(sDto);
        }

        return dto;
    }

    public List<OrdenDTO> listarTodasDTO() {
        return ordenRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
}
