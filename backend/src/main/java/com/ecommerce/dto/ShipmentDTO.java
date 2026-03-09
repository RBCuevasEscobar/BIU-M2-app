package com.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShipmentDTO {
    private Long id;
    private LocalDateTime dateDispatch;
    private LocalDateTime dateDelivered;
    private String courier;
    private String trackingNumber;
}
