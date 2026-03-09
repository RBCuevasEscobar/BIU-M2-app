package com.ecommerce.controller;

import com.ecommerce.dto.OrdenDTO;
import com.ecommerce.service.OrdenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    @Autowired
    private OrdenService ordenService;

    @PostMapping("/despachar/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdenDTO> despacharOrden(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String courier = body.get("courier");
        String trackingNumber = body.get("trackingNumber");

        OrdenDTO orden = ordenService.despacharOrden(id, courier, trackingNumber);
        return ResponseEntity.ok(orden);
    }

    @PostMapping("/entregar/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdenDTO> entregarOrden(@PathVariable Long id) {
        OrdenDTO orden = ordenService.entregarOrden(id);
        return ResponseEntity.ok(orden);
    }
}
