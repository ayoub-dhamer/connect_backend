package com.app.app.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @DecimalMin(value = "0.50", message = "Amount must be at least $0.50")
    @DecimalMax(value = "99999.99", message = "Amount must not exceed $99,999.99")
    private double amount;

    @NotBlank(message = "Product name is required")
    private String productName;
}