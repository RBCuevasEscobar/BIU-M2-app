package com.ecommerce.controller;

import com.ecommerce.dto.OrdenDTO;
import com.ecommerce.service.OrdenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private OrdenService ordenService;

    @PostMapping("/procesar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrdenDTO> procesarPago(@RequestBody Map<String, Object> body) {
        Long ordenId = Long.valueOf(body.get("ordenId").toString());
        String metodoPago = body.getOrDefault("metodoPago", "Tarjeta").toString();

        OrdenDTO ordenProcesada = ordenService.procesarPago(ordenId, metodoPago);
        return ResponseEntity.ok(ordenProcesada);
    }
}
