package com.ecommerce.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton de Configuracion Global.
 * Utilizado para acceder concurrentemente a parametros core del sistema eCommerce.
 * Es Thread-Safe utilizando objetos Atomicos que evitan Race Conditions.
 */
@Component
public class ConfiguracionSistema {

    private static volatile ConfiguracionSistema instance;

    // Parametros Centrales del E-Commerce (Thread-Safe state)
    private final AtomicReference<Double> iva = new AtomicReference<>(0.16);
    private final AtomicReference<String> monedaSistema = new AtomicReference<>("MXN");
    private final AtomicInteger stockMinimo = new AtomicInteger(5);
    private final AtomicBoolean modoDebug = new AtomicBoolean(false);
    private final AtomicInteger maxProductosOrden = new AtomicInteger(50);

    // Constructor privado para impedir instanciacion directa
    private ConfiguracionSistema() {}

    /**
     * DCL Double-Checked Locking initialization 
     */
    public static ConfiguracionSistema getInstance() {
        if (instance == null) {
            synchronized (ConfiguracionSistema.class) {
                if (instance == null) {
                    instance = new ConfiguracionSistema();
                }
            }
        }
        return instance;
    }

    // --- Getters y Setters Concurrencia ---

    public Double getIva() { return iva.get(); }
    public void setIva(Double iva) { this.iva.set(iva); }

    public String getMonedaSistema() { return monedaSistema.get(); }
    public void setMonedaSistema(String monedaSistema) { this.monedaSistema.set(monedaSistema); }

    public int getStockMinimo() { return stockMinimo.get(); }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo.set(stockMinimo); }

    public boolean isModoDebug() { return modoDebug.get(); }
    public void setModoDebug(boolean modoDebug) { this.modoDebug.set(modoDebug); }

    public int getMaxProductosOrden() { return maxProductosOrden.get(); }
    public void setMaxProductosOrden(int maxProductosOrden) { this.maxProductosOrden.set(maxProductosOrden); }
}
