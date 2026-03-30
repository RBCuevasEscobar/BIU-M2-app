package com.ecommerce.payment;

import com.ecommerce.model.Orden;
import com.ecommerce.model.PaymentTransaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("PagoTarjeta")
public class PagoTarjeta implements ProcesoPago {

    @Override
    public void iniciarPago(Orden orden) {
        System.out.println("Iniciando pago con Tarjeta de Crédito/Débito para la Orden: " + orden.getId());
    }

    @Override
    public boolean verificarPago(Orden orden) {
        // Lógica simulada: 95% de éxito. Aqui es donde se implementaria la logica de
        // pago real. Mientras tanto, se simula el pago.
        // Agregar proceso de generacion random de codigos de verificacion de pago con
        // tarjeta de acuerdo al estandar internacional.

        return Math.random() > 0.05;
    }

    @Override
    public PaymentTransaction confirmarPago(Orden orden, Double amount) {

        System.out.println("Pago con Tarjeta confirmado exitosamente.");
        return new PaymentTransaction("Tarjeta", "SUCCESS", amount, LocalDateTime.now());
    }
}
