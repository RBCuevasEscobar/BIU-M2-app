package com.ecommerce.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Factory para obtener la estrategia de pago correcta.
 */
@Service
public class ProcesoPagoFactory {

    @Autowired
    @Qualifier("PagoTarjeta")
    private ProcesoPago pagoTarjeta;

    @Autowired
    @Qualifier("PagoPayPal")
    private ProcesoPago pagoPayPal;

    @Autowired
    @Qualifier("PagoTransferencia")
    private ProcesoPago pagoTransferencia;

    public ProcesoPago obtenerMetodo(String method) {
        if (method == null)
            return pagoTarjeta;
        switch (method.toLowerCase()) {
            case "paypal":
                return pagoPayPal;
            case "transferencia":
                return pagoTransferencia;
            case "tarjeta":
            default:
                return pagoTarjeta;
        }
    }
}
