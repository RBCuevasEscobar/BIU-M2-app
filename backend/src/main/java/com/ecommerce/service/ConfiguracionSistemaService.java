package com.ecommerce.service;

import com.ecommerce.config.ConfiguracionSistema;
import com.ecommerce.model.SystemConfig;
import com.ecommerce.repository.SystemConfigRepository;
import com.ecommerce.security.JwtConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Servicio responsable de la persistencia y carga de configuración del sistema.
 *
 * Gestiona dos conjuntos de parámetros:
 * 1. Parámetros del Singleton ConfiguracionSistema (iva, monedaSistema, etc.)
 * 2. Parámetros JWT (jwt.expirationTime)
 *
 * La lógica interna del Singleton y de JwtConfig NO se modifica.
 * Este servicio actúa como capa de persistencia transparente.
 */
@Service
public class ConfiguracionSistemaService {

    // Claves de la tabla system_config
    private static final String KEY_IVA = "iva";
    private static final String KEY_MONEDA = "monedaSistema";
    private static final String KEY_STOCK_MINIMO = "stockMinimo";
    private static final String KEY_MODO_DEBUG = "modoDebug";
    private static final String KEY_MAX_PRODUCTOS = "maxProductosOrden";
    private static final String KEY_JWT_EXPIRATION = "jwt.expirationTime";

    private final SystemConfigRepository repo;
    private final JwtConfig jwtConfig;

    public ConfiguracionSistemaService(SystemConfigRepository repo, JwtConfig jwtConfig) {
        this.repo = repo;
        this.jwtConfig = jwtConfig;
    }

    /**
     * Devuelve una nueva instancia de ConfiguracionSistema con los valores actuales de la DB.
     */
    @Transactional(readOnly = true)
    public ConfiguracionSistema getConfiguracionSistema() {
        ConfiguracionSistema cs = new ConfiguracionSistema();
        leerDouble(KEY_IVA).ifPresent(cs::setIva);
        leerString(KEY_MONEDA).ifPresent(cs::setMonedaSistema);
        leerInt(KEY_STOCK_MINIMO).ifPresent(cs::setStockMinimo);
        leerBoolean(KEY_MODO_DEBUG).ifPresent(cs::setModoDebug);
        leerInt(KEY_MAX_PRODUCTOS).ifPresent(cs::setMaxProductosOrden);
        return cs;
    }

    /**
     * Carga todos los parámetros JWT.
     * Llamado por ConfiguracionSistemaLoader al arranque.
     */
    @Transactional(readOnly = true)
    public void cargarDesdeDB() {
        leerLong(KEY_JWT_EXPIRATION).ifPresent(jwtConfig::setExpirationTime);
    }

    /**
     * Persiste los parámetros del Singleton al recibir un PUT /api/config/sistema.
     * Solo persiste los campos presentes en el body.
     */
    @Transactional
    public void persistirSistema(Map<String, Object> body) {
        if (body.containsKey("iva"))
            guardar(KEY_IVA, body.get("iva").toString());
        if (body.containsKey("monedaSistema"))
            guardar(KEY_MONEDA, body.get("monedaSistema").toString());
        if (body.containsKey("stockMinimo"))
            guardar(KEY_STOCK_MINIMO, body.get("stockMinimo").toString());
        if (body.containsKey("modoDebug"))
            guardar(KEY_MODO_DEBUG, body.get("modoDebug").toString());
        if (body.containsKey("maxProductosOrden"))
            guardar(KEY_MAX_PRODUCTOS, body.get("maxProductosOrden").toString());
    }

    /**
     * Persiste el tiempo de expiración JWT al recibir un PUT /api/config/jwt.
     */
    @Transactional
    public void persistirJwt(long expirationTime) {
        guardar(KEY_JWT_EXPIRATION, String.valueOf(expirationTime));
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private void guardar(String key, String value) {
        repo.save(new SystemConfig(key, value));
    }

    private Optional<Double> leerDouble(String key) {
        return repo.findById(key).map(sc -> {
            try {
                return Double.parseDouble(sc.getValue());
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    private Optional<String> leerString(String key) {
        return repo.findById(key).map(SystemConfig::getValue);
    }

    private Optional<Integer> leerInt(String key) {
        return repo.findById(key).map(sc -> {
            try {
                return Integer.parseInt(sc.getValue());
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    private Optional<Long> leerLong(String key) {
        return repo.findById(key).map(sc -> {
            try {
                return Long.parseLong(sc.getValue());
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    private Optional<Boolean> leerBoolean(String key) {
        return repo.findById(key).map(sc -> Boolean.parseBoolean(sc.getValue()));
    }
}
