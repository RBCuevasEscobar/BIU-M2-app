package com.ecommerce.service;

import com.ecommerce.config.ConfiguracionSistema;
import com.ecommerce.dto.OrdenDTO;
import com.ecommerce.dto.OrdenDetalleDTO;
import com.ecommerce.dto.PaymentTransactionDTO;
import com.ecommerce.dto.ShipmentDTO;
import com.ecommerce.inventory.GestorInventario;
import com.ecommerce.inventory.GestorInventarioFactory;
import com.ecommerce.model.*;
import com.ecommerce.observer.events.MaxItemsExcedidoEvent;
import com.ecommerce.payment.ProcesoPago;
import com.ecommerce.payment.ProcesoPagoFactory;
import com.ecommerce.repository.CarritoRepository;
import com.ecommerce.repository.OrdenRepository;
import com.ecommerce.repository.PaymentTransactionRepository;
import com.ecommerce.repository.ShipmentRepository;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.repository.DireccionRepository;
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
    private DireccionRepository direccionRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private GestorInventarioFactory inventarioFactory;

    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Autowired
    private ProcesoPagoFactory pagoFactory;

    private Usuario getUsuarioActual() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        System.out.println("Email: " + email);
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * FASE 1: Creación de la Orden (CREATED)
     * Convierte el carrito actual en una nueva orden en estado CREATED.
     * Selecciona una dirección de envío si direccionId no es nulo.
     */
    @Transactional
    public OrdenDTO crearOrdenDesdeCarrito(Long direccionId) {
        Usuario usuario = getUsuarioActual();
        Carrito carrito = carritoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (carrito.getProductos().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Validar máximo de ítems por orden (Req. 10)
        ConfiguracionSistema config = ConfiguracionSistema.getInstance();
        int maxItems = config.getMaxProductosOrden();
        int totalItems = carrito.getProductos().size();
        if (totalItems >= maxItems) {
            // Publicar evento para notificación al ADMIN
            eventPublisher.publishEvent(new MaxItemsExcedidoEvent(this, usuario, totalItems, maxItems));
            throw new RuntimeException("MAX_ITEMS_EXCEDIDO:" + totalItems + ":" + maxItems);
        }

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setEstado(EstadoOrden.CREATED);

        if (direccionId != null) {
            Direccion direccion = direccionRepository.findById(direccionId)
                    .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
            if (!direccion.getUsuario().getId().equals(usuario.getId())) {
                throw new RuntimeException("La dirección no pertenece a este usuario");
            }
            orden.setDireccionEnvio(direccion);
        }

        Map<Producto, Long> conteoProductos = carrito.getProductos().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        double subtotalProductos = 0.0;
        for (Map.Entry<Producto, Long> entry : conteoProductos.entrySet()) {
            Producto producto = entry.getKey();
            Integer cantidad = entry.getValue().intValue();
            Double subtotal = producto.getPrecio() * cantidad;

            OrdenDetalle detalle = new OrdenDetalle(cantidad, subtotal, producto);
            orden.agregarDetalle(detalle);
            subtotalProductos += subtotal;
        }

        // Persistir IVA del sistema al momento de la creación (Req. 2)
        double ivaTasa = config.getIva();
        double total = subtotalProductos * (1.0 + ivaTasa);

        orden.setSubtotalProductos(subtotalProductos);
        orden.setIvaTasa(ivaTasa);
        orden.setTotal(total);

        // Limpiar carrito
        carrito.getProductos().clear();
        carritoRepository.save(carrito);

        Orden guardada = ordenRepository.save(orden);
        eventPublisher.publishEvent(new com.ecommerce.observer.events.OrdenCreadaEvent(this, guardada));

        return toDTO(guardada);
    }

    /**
     * FASE 2: Procesar Pago y Verificar Stock
     */
    @Transactional
    public OrdenDTO procesarPago(Long ordenId, String metodoPago, Long direccionId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (direccionId != null) {
            Direccion direccion = direccionRepository.findById(direccionId)
                    .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
            if (!direccion.getUsuario().getId().equals(orden.getUsuario().getId())) {
                throw new RuntimeException("La dirección no pertenece al propietario de la orden");
            }
            orden.setDireccionEnvio(direccion);
        }

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

        // 2. Actualizar ivaTasa si cambió desde la creación de la orden (Req. 2, 6)
        ConfiguracionSistema configActual = ConfiguracionSistema.getInstance();
        double ivaTasaActual = configActual.getIva();
        if (orden.getIvaTasa() == null || Double.compare(orden.getIvaTasa(), ivaTasaActual) != 0) {
            // La tasa cambió — recalcular total con nueva tasa
            double subtotal = orden.getSubtotalProductos() != null
                    ? orden.getSubtotalProductos()
                    : calcularSubtotalProductos(orden);
            orden.setSubtotalProductos(subtotal);
            orden.setIvaTasa(ivaTasaActual);
            orden.setTotal(subtotal * (1.0 + ivaTasaActual));
        }

        // 3. Procesar Pago (OOP Interface)
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

            // Persistir RFC del cliente al momento del pago exitoso (Req. 11)
            if (orden.getUsuario() instanceof Cliente cliente) {
                orden.setRfcCliente(cliente.getRfcCurp());
            }

            // 4. Descontar Stock definitivo y Auditorias delegadas a Observadores
            eventPublisher.publishEvent(new com.ecommerce.observer.events.OrdenPagadaEvent(this, orden));
        } else {
            // Se queda en pending si falla
            orden.setEstado(EstadoOrden.PAYMENT_PENDING);
        }

        return toDTO(ordenRepository.save(orden));
    }

    /**
     * Marcar Orden como Pendiente de Pago (Transición Checkout)
     */
    @Transactional
    public OrdenDTO marcarComoPendiente(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        validarPropietarioOAdmin(orden);

        if (orden.getEstado() == EstadoOrden.CREATED) {
            orden.setEstado(EstadoOrden.PAYMENT_PENDING);
        } else if (orden.getEstado() != EstadoOrden.PAYMENT_PENDING) {
            throw new RuntimeException("La orden debe estar en estado CREATED para iniciar el pago.");
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
        Orden guardada = ordenRepository.save(orden);

        eventPublisher.publishEvent(new com.ecommerce.observer.events.OrdenCanceladaEvent(this, guardada));

        return toDTO(guardada);
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

        Orden guardada = ordenRepository.save(orden);
        eventPublisher.publishEvent(new com.ecommerce.observer.events.OrdenDespachadaEvent(this, guardada));

        return toDTO(guardada);
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

        Orden guardada = ordenRepository.save(orden);
        eventPublisher.publishEvent(new com.ecommerce.observer.events.OrdenEntregadaEvent(this, guardada));

        return toDTO(guardada);
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

    /**
     * Calcula el subtotal de productos desde los detalles (fallback para órdenes antiguas
     * que no tienen subtotalProductos persistido).
     */
    private double calcularSubtotalProductos(Orden orden) {
        return orden.getDetalles().stream()
                .mapToDouble(d -> d.getSubtotal())
                .sum();
    }

    private OrdenDTO toDTO(Orden orden) {

        OrdenDTO dto = new OrdenDTO();

        dto.setId(orden.getId());
        dto.setEstado(orden.getEstado().name());
        dto.setFechaCreacion(orden.getFechaCreacion());
        dto.setUltimaModificacion(orden.getUltimaModificacion());

        dto.setUsuarioId(orden.getUsuario().getId());
        dto.setUsuarioEmail(orden.getUsuario().getEmail());
        dto.setUsuarioNombre(orden.getUsuario().getNombre());
        if (orden.getUsuario() instanceof com.ecommerce.model.Cliente cliente) {
            dto.setUsuarioRfcCurp(cliente.getRfcCurp());
        }

        // Subtotal de productos (sin IVA) — calcula desde detalles si aún no está persistido
        double subtotalProductos = orden.getSubtotalProductos() != null
                ? orden.getSubtotalProductos()
                : calcularSubtotalProductos(orden);
        dto.setSubtotalProductos(subtotalProductos);

        // IVA: usa la guardada en la orden; si no existe (órdenes antiguas) usa la del sistema
        double ivaTasa = orden.getIvaTasa() != null
                ? orden.getIvaTasa()
                : ConfiguracionSistema.getInstance().getIva();
        dto.setIvaTasa(ivaTasa);
        dto.setMontoImpuestos(subtotalProductos * ivaTasa);

        // Total: usa el total guardado en la orden (ya incluye IVA)
        dto.setTotal(orden.getTotal() != null ? orden.getTotal() : subtotalProductos * (1.0 + ivaTasa));

        // RFC del cliente: usa el RFC guardado en la orden (post-PAID) o el del usuario
        if (orden.getRfcCliente() != null) {
            dto.setRfcCliente(orden.getRfcCliente());
        }

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

        if (orden.getDireccionEnvio() != null) {
            dto.setDireccionEnvio(com.ecommerce.dto.DireccionDTO.fromEntity(orden.getDireccionEnvio()));
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
