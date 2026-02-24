package com.ecommerce.controller;

import com.ecommerce.model.Carrito;
import com.ecommerce.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping
    public ResponseEntity<Carrito> obtenerCarrito() {
        return ResponseEntity.ok(carritoService.obtenerCarritoActual());
    }

    @PostMapping("/productos/{productoId}")
    public ResponseEntity<Carrito> agregarProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(carritoService.agregarProducto(productoId));
    }

    @DeleteMapping("/productos/{productoId}")
    public ResponseEntity<Carrito> eliminarProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(carritoService.eliminarProducto(productoId));
    }

    // 🔥 NUEVO ENDPOINT
    @DeleteMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Carrito> vaciarCarrito() {
        return ResponseEntity.ok(carritoService.vaciarCarrito());
    }

}
