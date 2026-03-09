package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paymentMethod; // e.g. "Tarjeta", "PayPal", "Transferencia"

    @Column(nullable = false)
    private String status; // e.g. "SUCCESS", "FAILED"

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @OneToOne(mappedBy = "paymentTransaction")
    @JsonIgnore
    private Orden orden;

    public PaymentTransaction(String paymentMethod, String status, Double amount, LocalDateTime transactionDate) {
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }
}
