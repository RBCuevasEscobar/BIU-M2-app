package com.ecommerce.controller;

import com.ecommerce.config.ConfiguracionSistema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de Gestión Administrativa del Singleton Central.
 */
@RestController
@RequestMapping("/api/config/sistema")
public class ConfigSistemaController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfiguracion() {
        ConfiguracionSistema cs = ConfiguracionSistema.getInstance();
        return ResponseEntity.ok(Map.of(
                "iva", cs.getIva(),
                "monedaSistema", cs.getMonedaSistema(),
                "stockMinimo", cs.getStockMinimo(),
                "modoDebug", cs.isModoDebug(),
                "maxProductosOrden", cs.getMaxProductosOrden()
        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateConfiguracion(@RequestBody Map<String, Object> body) {
        ConfiguracionSistema cs = ConfiguracionSistema.getInstance();

        if (body.containsKey("iva")) {
            cs.setIva(Double.parseDouble(body.get("iva").toString()));
        }
        if (body.containsKey("monedaSistema")) {
            cs.setMonedaSistema(body.get("monedaSistema").toString());
        }
        if (body.containsKey("stockMinimo")) {
            cs.setStockMinimo(Integer.parseInt(body.get("stockMinimo").toString()));
        }
        if (body.containsKey("modoDebug")) {
            cs.setModoDebug(Boolean.parseBoolean(body.get("modoDebug").toString()));
        }
        if (body.containsKey("maxProductosOrden")) {
            cs.setMaxProductosOrden(Integer.parseInt(body.get("maxProductosOrden").toString()));
        }

        return ResponseEntity.ok(Map.of("message", "Configuración del Sistema actualizada con éxito."));
    }
}
