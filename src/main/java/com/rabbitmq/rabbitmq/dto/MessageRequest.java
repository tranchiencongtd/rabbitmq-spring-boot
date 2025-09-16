package com.rabbitmq.rabbitmq.dto;

import java.util.Map;

public class MessageRequest {
    private String exchangeType;  // direct, fanout, topic, headers
    private String routingKey;    // dùng cho direct, topic
    private String message;       // nội dung message
    private Map<String, String> headers; // cho headers exchange

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
