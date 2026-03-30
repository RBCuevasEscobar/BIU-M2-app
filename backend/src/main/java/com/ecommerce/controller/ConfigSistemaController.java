package com.ecommerce.controller;

import com.ecommerce.config.ConfiguracionSistema;
import com.ecommerce.service.ConfiguracionSistemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de Gestión Administrativa del Singleton Central.
 * Ahora incluye persistencia transparente en DB via
 * ConfiguracionSistemaService.
 */
@RestController
@RequestMapping("/api/config/sistema")
public class ConfigSistemaController {

    private final ConfiguracionSistemaService configService;

    public ConfigSistemaController(ConfiguracionSistemaService configService) {
        this.configService = configService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfiguracion() {
        ConfiguracionSistema cs = configService.getConfiguracionSistema();
        return ResponseEntity.ok(Map.of(
                "iva", cs.getIva(),
                "monedaSistema", cs.getMonedaSistema(),
                "stockMinimo", cs.getStockMinimo(),
                "modoDebug", cs.isModoDebug(),
                "maxProductosOrden", cs.getMaxProductosOrden()));
    }

    /**
     * Endpoint público sin autenticación: expone solo iva, monedaSistema y
     * maxProductosOrden
     * para que los componentes frontend (carrito, checkout) los consuman sin ser
     * ADMIN.
     */
    @GetMapping("/publica")
    public ResponseEntity<Map<String, Object>> getConfiguracionPublica() {
        ConfiguracionSistema cs = configService.getConfiguracionSistema();
        return ResponseEntity.ok(Map.of(
                "iva", cs.getIva(),
                "monedaSistema", cs.getMonedaSistema(),
                "stockMinimo", cs.getStockMinimo(),
                "modoDebug", cs.isModoDebug(),
                "maxProductosOrden", cs.getMaxProductosOrden()));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateConfiguracion(@RequestBody Map<String, Object> body) {
        ConfiguracionSistema cs = configService.getConfiguracionSistema();

        // 1. Actualizar Singleton en memoria (lógica original preservada)
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

        // 2. Persistir en DB (nueva funcionalidad, no altera la lógica anterior)
        configService.persistirSistema(body);

        return ResponseEntity.ok(Map.of("message", "Configuración del Sistema actualizada con éxito."));
    }
}
