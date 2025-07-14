package com.neosburritos.service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Payment processing service
 * Simulates payment processing with different payment methods
 */
public class PaymentService {
    
    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        PAYPAL("PayPal"),
        CASH_ON_DELIVERY("Cash on Delivery");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Result class for payment operations
     */
    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final String transactionId;
        
        public PaymentResult(boolean success, String message, String transactionId) {
            this.success = success;
            this.message = message;
            this.transactionId = transactionId;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
    }
    
    private final Random random = new Random();
    
    /**
     * Process payment for an order
     */
    public PaymentResult processPayment(int orderId, BigDecimal amount, String currencyCode, 
                                      PaymentMethod method, String paymentDetails) {
        
        // Simulate payment processing delay
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate payment success/failure (90% success rate)
        boolean success = random.nextDouble() < 1;
        
        if (success) {
            String transactionId = generateTransactionId();
            String message = String.format("Payment of %s %s processed successfully via %s", 
                                          currencyCode, amount.toString(), method.getDisplayName());
            
            System.out.println("Payment processed: " + message + " (Transaction ID: " + transactionId + ")");
            return new PaymentResult(true, message, transactionId);
            
        } else {
            String message = "Payment failed. Please check your payment details and try again.";
            System.out.println("Payment failed for order " + orderId);
            return new PaymentResult(false, message, null);
        }
    }
    
    /**
     * Validate payment details based on payment method
     */
    public boolean validatePaymentDetails(PaymentMethod method, String paymentDetails) {
        if (paymentDetails == null || paymentDetails.trim().isEmpty()) {
            return false;
        }
        
        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                // Simple validation: should be 16 digits
                return paymentDetails.replaceAll("\\s", "").matches("\\d{16}");
                
            case PAYPAL:
                // Simple email validation
                return paymentDetails.contains("@") && paymentDetails.contains(".");
                
            case CASH_ON_DELIVERY:
                // No validation needed for COD
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Get payment method requirements text
     */
    public String getPaymentMethodRequirements(PaymentMethod method) {
        switch (method) {
            case CREDIT_CARD:
                return "Enter 16-digit credit card number (e.g., 1234 5678 9012 3456)";
            case DEBIT_CARD:
                return "Enter 16-digit debit card number (e.g., 1234 5678 9012 3456)";
            case PAYPAL:
                return "Enter PayPal email address";
            case CASH_ON_DELIVERY:
                return "No payment details required - pay when order is delivered";
            default:
                return "Enter payment details";
        }
    }
    
    /**
     * Generate a random transaction ID
     */
    private String generateTransactionId() {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return "TXN" + sb.toString();
    }
    
    /**
     * Refund a payment (simulation)
     */
    public PaymentResult refundPayment(String transactionId, BigDecimal amount, String currencyCode) {
        // Simulate refund processing
        try {
            Thread.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate refund success (95% success rate)
        boolean success = random.nextDouble() < 0.95;
        
        if (success) {
            String refundId = generateTransactionId();
            String message = String.format("Refund of %s %s processed successfully", 
                                          currencyCode, amount.toString());
            
            System.out.println("Refund processed: " + message + " (Refund ID: " + refundId + ")");
            return new PaymentResult(true, message, refundId);
            
        } else {
            String message = "Refund failed. Please contact customer support.";
            System.out.println("Refund failed for transaction " + transactionId);
            return new PaymentResult(false, message, null);
        }
    }
}