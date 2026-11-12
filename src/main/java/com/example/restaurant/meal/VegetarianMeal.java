package com.example.restaurant.meal;

import java.util.Objects;

public record VegetarianMeal(MealType type) implements Meal {
    public VegetarianMeal {
        Objects.requireNonNull(type);
    }

    @Override public String description() {
        return switch (type) {
            case PIZZA -> "Vegetarian pizza";
            case BURGER -> "Veggie burger";
            case PASTA -> "Vegetable pasta";
        };
    }

    @Override public long priceCents() {
        return switch (type) {
            case PIZZA -> 1300;
            case BURGER -> 950;
            case PASTA -> 1150;
        };
    }
}
