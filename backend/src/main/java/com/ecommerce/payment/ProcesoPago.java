package com.ecommerce.payment;

import com.ecommerce.model.Orden;
import com.ecommerce.model.PaymentTransaction;

/**
 * Interfaz que define el contrato para procesar pagos.
 * Demuestra los principios OOP de Interfaces y Polimorfismo.
 */
public interface ProcesoPago {

    /**
     * Inicia el proceso de pago.
     * 
     * @param orden La orden a pagar
     */
    void iniciarPago(Orden orden);

    /**
     * Verifica asíncronamente si el pago fue exitoso o falló.
     * 
     * @param orden La orden sujeta verificación
     * @return true si el pago está aprobado, false si falló o sigue pendiente
     */
    boolean verificarPago(Orden orden);

    /**
     * Consolida el pago y genera la transacción resultante.
     * 
     * @param orden  La orden pagada.
     * @param amount Monto consolidado.
     * @return Transacción de pago registrada
     */
    PaymentTransaction confirmarPago(Orden orden, Double amount);
}
