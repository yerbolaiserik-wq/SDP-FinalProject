package com.example.restaurant.strategy;

public interface DiscountStrategy {
    String name();
    long discountCents(long subtotalCents);
}
