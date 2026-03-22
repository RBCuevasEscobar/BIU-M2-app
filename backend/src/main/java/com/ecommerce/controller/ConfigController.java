package com.ecommerce.controller;

import com.ecommerce.security.JwtConfig;
import com.ecommerce.service.ConfiguracionSistemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config/jwt")
public class ConfigController {

    private final JwtConfig jwtConfig;
    private final ConfiguracionSistemaService configService;

    public ConfigController(JwtConfig jwtConfig, ConfiguracionSistemaService configService) {
        this.jwtConfig = jwtConfig;
        this.configService = configService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getJwtConfig() {
        return ResponseEntity.ok(Map.of(
                "expirationTime", jwtConfig.getExpirationTime()
        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateJwtConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("expirationTime")) {
            long newExpiration = Long.parseLong(body.get("expirationTime").toString());

            // 1. Actualizar JwtConfig en memoria (lógica original preservada)
            jwtConfig.setExpirationTime(newExpiration);

            // 2. Persistir en DB (nueva funcionalidad)
            configService.persistirJwt(newExpiration);

            return ResponseEntity.ok(Map.of(
                    "message", "Configuración JWT actualizada",
                    "expirationTime", newExpiration
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Falta el parámetro expirationTime"));
    }
}
