package com.example.restaurant.strategy;

public final class StudentDiscount implements DiscountStrategy {
    public String name() { return "Student 10%"; }
    public long discountCents(long subtotalCents) {
        if (subtotalCents < 0) { throw new IllegalArgumentException("Subtotal cannot be negative."); }
        return subtotalCents / 10;
    }
}
