package com.example.restaurant.factory;

import com.example.restaurant.meal.Meal;
import com.example.restaurant.meal.MealType;
import com.example.restaurant.meal.VegetarianMeal;
import java.util.Objects;

public final class VegetarianMealFactory extends MealFactory {
    private final MealType type;

    public VegetarianMealFactory(MealType type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override public Meal createMeal() { return new VegetarianMeal(type); }
}
