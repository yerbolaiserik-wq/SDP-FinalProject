package com.example.restaurant.strategy;

public final class LoyaltyDiscount implements DiscountStrategy {
    public String name() { return "Loyalty 15%"; }
    public long discountCents(long subtotalCents) {
        if (subtotalCents < 0) { throw new IllegalArgumentException("Subtotal cannot be negative."); }
        return Math.multiplyExact(subtotalCents, 15) / 100;
    }
}
