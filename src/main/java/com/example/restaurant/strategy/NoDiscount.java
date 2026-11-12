package com.example.restaurant.strategy;

public final class NoDiscount implements DiscountStrategy {
    public String name() { return "No discount"; }
    public long discountCents(long subtotalCents) {
        if (subtotalCents < 0) { throw new IllegalArgumentException("Subtotal cannot be negative."); }
        return 0;
    }
}
