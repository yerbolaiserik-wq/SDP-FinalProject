package com.example.restaurant.factory;

import com.example.restaurant.meal.*;

public final class PizzaFactory extends MealFactory {
    @Override public Meal createMeal() { return new Pizza(); }
}
