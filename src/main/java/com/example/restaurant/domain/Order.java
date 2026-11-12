package com.example.restaurant.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record Order(long id, String customer, List<OrderLine> items, String discountName,
                    long discountCents, OrderStatus status, Instant createdAt) {
    public Order {
        if (id < 1) { throw new IllegalArgumentException("Order number must be positive."); }
        Objects.requireNonNull(customer);
        customer = customer.trim();
        if (customer.isBlank() || customer.length() > 20) {
            throw new IllegalArgumentException("Enter a customer name of 1 to 20 characters.");
        }
        items = List.copyOf(items);
        if (items.isEmpty() || items.size() > 50) {
            throw new IllegalArgumentException("An order must contain 1 to 50 basket lines.");
        }
        Objects.requireNonNull(discountName);
        Objects.requireNonNull(status);
        Objects.requireNonNull(createdAt);
        long subtotal = 0;
        for (OrderLine item : items) { subtotal = Math.addExact(subtotal, item.totalCents()); }
        if (discountCents < 0 || discountCents > subtotal) {
            throw new IllegalArgumentException("Discount must be between zero and the subtotal.");
        }
    }
    public long subtotalCents() {
        long subtotal = 0;
        for (OrderLine item : items) { subtotal = Math.addExact(subtotal, item.totalCents()); }
        return subtotal;
    }
    public long totalCents() { return subtotalCents() - discountCents; }
    public Order withStatus(OrderStatus next) {
        Objects.requireNonNull(next);
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("Cannot change " + status + " to " + next + ".");
        }
        return new Order(id, customer, items, discountName, discountCents, next, createdAt);
    }
    public String number() { return String.format(java.util.Locale.ROOT, "#%04d", id); }
}
