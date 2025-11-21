package com.staykonnect.dto.pago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para webhooks de Stripe.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeWebhookEvent {

    private String type; // payment_intent.succeeded, payment_intent.payment_failed, etc.
    private String paymentIntentId;
    private String status;
    private Long amount;
    private String currency;
}
