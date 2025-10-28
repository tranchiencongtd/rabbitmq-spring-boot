package com.rabbitmq.rabbitmq.dto;

public class CreateOrderRequest {
    private String userId;
    private Double amount;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String userId, Double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
