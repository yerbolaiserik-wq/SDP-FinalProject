package com.example.restaurant.decorator;

import com.example.restaurant.meal.Meal;
import java.util.Objects;

public abstract class MealDecorator implements Meal {
    protected final Meal meal;

    protected MealDecorator(Meal meal) {
        this.meal = Objects.requireNonNull(meal);
    }
}
