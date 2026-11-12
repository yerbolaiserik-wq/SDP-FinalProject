package com.example.restaurant.factory;

import com.example.restaurant.meal.Meal;

public abstract class MealFactory {
    public abstract Meal createMeal();

    public String menuDescription() {
        Meal meal = createMeal();
        return meal.description();
    }
}
