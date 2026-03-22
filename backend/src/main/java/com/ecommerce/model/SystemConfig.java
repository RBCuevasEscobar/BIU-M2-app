package com.ecommerce.model;

import jakarta.persistence.*;

/**
 * Entidad JPA para la persistencia de configuraciones del sistema.
 *
 * Almacena pares clave-valor en la tabla system_config.
 * Utilizada por ConfiguracionSistemaService para persistir y cargar:
 *  - Parámetros del Singleton ConfiguracionSistema (iva, moneda, etc.)
 *  - Parámetros JWT (jwt.expirationTime)
 */
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @Column(name = "`key`", length = 100, nullable = false)
    private String key;

    @Column(name = "value", length = 255)
    private String value;

    public SystemConfig() {}

    public SystemConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
