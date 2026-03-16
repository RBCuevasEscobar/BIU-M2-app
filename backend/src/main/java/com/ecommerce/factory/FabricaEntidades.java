package com.ecommerce.factory;

import com.ecommerce.model.*;
import org.springframework.stereotype.Component;

/**
 * Fabrica de Entidades Centralizada (Factory Pattern).
 * Desacopla la logica de inicializacion y polimorfismo de controladores y servicios.
 */
@Component
public class FabricaEntidades {

    /**
     * Instancia Usuarios polimorficos en funcion de su Rol
     */
    public Usuario crearUsuarioSegunRol(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo para instanciar un usuario");
        }
        
        return switch (role) {
            case ADMIN -> new Administrador();
            case SUPPLIER -> new Proveedor();
            case CUSTOMER -> new Cliente();
        };
    }

    /**
     * Instancia Productos polimorficos basados en su directiva tipo
     */
    public Producto crearProductoSegunTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de producto no puede estar vacio");
        }

        if (tipo.equalsIgnoreCase("Fisico")) {
            return new ProductoFisico();
        } else if (tipo.equalsIgnoreCase("Digital")) {
            return new ProductoDigital();
        } else {
            throw new IllegalArgumentException("Tipo de producto no soportado por fabrica: " + tipo);
        }
    }
}
