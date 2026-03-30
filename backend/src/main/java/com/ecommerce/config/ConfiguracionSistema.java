package com.ecommerce.config;

import org.springframework.stereotype.Component;

/**
 * Modelo DTO de Configuracion Global.
 * Convertido para ser compatible en ambientes Cloud-Native.
 */
@Component
public class ConfiguracionSistema {

    private Double iva = 0.16;
    private String monedaSistema = "MXN";
    private int stockMinimo = 5;
    private boolean modoDebug = false;
    private int maxProductosOrden = 50;

    public ConfiguracionSistema() {}

    public Double getIva() { return iva; }
    public void setIva(Double iva) { this.iva = iva; }

    public String getMonedaSistema() { return monedaSistema; }
    public void setMonedaSistema(String monedaSistema) { this.monedaSistema = monedaSistema; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isModoDebug() { return modoDebug; }
    public void setModoDebug(boolean modoDebug) { this.modoDebug = modoDebug; }

    public int getMaxProductosOrden() { return maxProductosOrden; }
    public void setMaxProductosOrden(int maxProductosOrden) { this.maxProductosOrden = maxProductosOrden; }
}
