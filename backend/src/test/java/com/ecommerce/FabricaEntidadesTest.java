package com.ecommerce;

import com.ecommerce.factory.FabricaEntidades;
import com.ecommerce.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FabricaEntidadesTest {

    private final FabricaEntidades fabrica = new FabricaEntidades();

    @Test
    public void testCrearUsuario_Cliente() {
        Usuario u = fabrica.crearUsuarioSegunRol(Role.CUSTOMER);
        assertTrue(u instanceof Cliente, "Debe ser de tipo Cliente");
    }

    @Test
    public void testCrearUsuario_Proveedor() {
        Usuario u = fabrica.crearUsuarioSegunRol(Role.SUPPLIER);
        assertTrue(u instanceof Proveedor, "Debe ser de tipo Proveedor");
    }

    @Test
    public void testCrearUsuario_Administrador() {
        Usuario u = fabrica.crearUsuarioSegunRol(Role.ADMIN);
        assertTrue(u instanceof Administrador, "Debe ser de tipo Administrador");
    }

    @Test
    public void testCrearProducto_Fisico() {
        Producto p = fabrica.crearProductoSegunTipo("Fisico");
        assertTrue(p instanceof ProductoFisico, "Debe ser ProductoFisico");
    }

    @Test
    public void testCrearProducto_Digital() {
        Producto p = fabrica.crearProductoSegunTipo("Digital");
        assertTrue(p instanceof ProductoDigital, "Debe ser ProductoDigital");
    }

    @Test
    public void testCrearProducto_Invalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            fabrica.crearProductoSegunTipo("NoExistente");
        });
    }
}
