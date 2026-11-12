package com.example.restaurant.domain;

import com.example.restaurant.meal.Meal;
import com.example.restaurant.menu.Drink;
import java.util.Objects;

public record OrderLine(String description, int quantity, long unitPriceCents) {
    public OrderLine {
        Objects.requireNonNull(description);
        if (description.isBlank()) { throw new IllegalArgumentException("Meal description is required."); }
        if (quantity < 1 || quantity > 20) { throw new IllegalArgumentException("Quantity must be between 1 and 20."); }
        if (unitPriceCents < 0) { throw new IllegalArgumentException("Meal price cannot be negative."); }
        Math.multiplyExact(unitPriceCents, quantity);
    }
    public static OrderLine from(Meal meal, int quantity) {
        Objects.requireNonNull(meal);
        return new OrderLine(meal.description(), quantity, meal.priceCents());
    }
    public static OrderLine from(Drink drink, int quantity) {
        Objects.requireNonNull(drink);
        return new OrderLine(drink.description(), quantity, drink.priceCents());
    }
    public long totalCents() { return Math.multiplyExact(unitPriceCents, quantity); }
}
