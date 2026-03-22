package com.ecommerce.config;

import com.ecommerce.service.ConfiguracionSistemaService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Cargador de configuración del sistema al inicio de la aplicación.
 *
 * Se activa con ApplicationReadyEvent (después de que Spring ha inicializado
 * todos los beans y el contexto está completamente listo), garantizando que
 * la base de datos y el repositorio estén disponibles antes de leer la config.
 *
 * Aplica los valores persistidos en system_config al Singleton
 * ConfiguracionSistema y a JwtConfig.
 */
@Component
public class ConfiguracionSistemaLoader {

    private final ConfiguracionSistemaService service;

    public ConfiguracionSistemaLoader(ConfiguracionSistemaService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("[CONFIG-LOADER] Aplicación lista. Cargando configuración desde DB...");
        service.cargarDesdeDB();
    }
}
