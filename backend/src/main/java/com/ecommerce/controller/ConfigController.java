package com.ecommerce.controller;

import com.ecommerce.security.JwtConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config/jwt")
public class ConfigController {

    private final JwtConfig jwtConfig;

    public ConfigController(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
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
            jwtConfig.setExpirationTime(newExpiration);
            return ResponseEntity.ok(Map.of("message", "Configuración JWT actualizada", "expirationTime", newExpiration));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Falta el parámetro expirationTime"));
    }
}
