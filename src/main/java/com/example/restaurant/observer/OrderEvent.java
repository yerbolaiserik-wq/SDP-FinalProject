package com.example.restaurant.observer;

import com.example.restaurant.domain.Order;
import com.example.restaurant.domain.OrderStatus;
import java.time.Instant;
import java.util.Objects;

public record OrderEvent(Order order, OrderStatus previousStatus, Instant occurredAt) {
    public OrderEvent {
        Objects.requireNonNull(order);
        Objects.requireNonNull(occurredAt);
    }
}
