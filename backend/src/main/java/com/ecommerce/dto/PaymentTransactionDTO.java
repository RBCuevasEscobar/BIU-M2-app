package com.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentTransactionDTO {
    private Long id;
    private String paymentMethod;
    private String status;
    private Double amount;
    private LocalDateTime transactionDate;
}
