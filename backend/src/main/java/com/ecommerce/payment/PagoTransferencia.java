package com.ecommerce.payment;

import com.ecommerce.model.Orden;
import com.ecommerce.model.PaymentTransaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("PagoTransferencia")
public class PagoTransferencia implements ProcesoPago {

    @Override
    public void iniciarPago(Orden orden) {
        System.out.println("Generando instrucciones de Transferencia Bancaria para Orden: " + orden.getId());
    }

    @Override
    public boolean verificarPago(Orden orden) {
        // Lógica simulada: asume verificación manual rápida por admin
        return true;
    }

    @Override
    public PaymentTransaction confirmarPago(Orden orden, Double amount) {
        System.out.println("Pago por Transferencia validado.");
        return new PaymentTransaction("Transferencia", "SUCCESS", amount, LocalDateTime.now());
    }
}
