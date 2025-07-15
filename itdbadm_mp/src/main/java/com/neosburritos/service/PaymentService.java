package com.neosburritos.service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Simplified Payment processing service
 * Simulates payment processing without unnecessary payment method complexity
 */
public class PaymentService {
    
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
     * Process payment for an order (simplified)
     */
    public PaymentResult processPayment(int orderId, BigDecimal amount, String currencyCode) {
        
        // Simulate payment processing delay
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate payment success/failure (95% success rate)
        boolean success = random.nextDouble() < 0.95;
        
        if (success) {
            String transactionId = generateTransactionId();
            String message = String.format("Payment of %s %s processed successfully", 
                                          currencyCode, amount.toString());
            
            System.out.println("Payment processed: " + message + " (Transaction ID: " + transactionId + ")");
            return new PaymentResult(true, message, transactionId);
            
        } else {
            String message = "Payment failed. Please try again or contact support.";
            System.out.println("Payment failed for order " + orderId);
            return new PaymentResult(false, message, null);
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