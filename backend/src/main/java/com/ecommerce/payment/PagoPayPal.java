package com.ecommerce.payment;

import com.ecommerce.model.Orden;
import com.ecommerce.model.PaymentTransaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("PagoPayPal")
public class PagoPayPal implements ProcesoPago {

    @Override
    public void iniciarPago(Orden orden) {
        System.out.println("Iniciando redirección a PayPal para la Orden: " + orden.getId());
    }

    @Override
    public boolean verificarPago(Orden orden) {
        // Lógica simulada
        return Math.random() > 0.05;
    }

    @Override
    public PaymentTransaction confirmarPago(Orden orden, Double amount) {
        System.out.println("Pago con PayPal confirmado exitosamente.");
        return new PaymentTransaction("PayPal", "SUCCESS", amount, LocalDateTime.now());
    }
}
