package com.example.restaurant.menu;

import com.example.restaurant.factory.MealFactory;
import com.example.restaurant.factory.VegetarianMealFactory;
import com.example.restaurant.meal.Meal;
import com.example.restaurant.meal.MealType;
import java.util.Map;
import java.util.Objects;

public final class VegetarianMenuFactory implements MenuFactory {
    private final Map<MealType, MealFactory> meals = Map.of(
            MealType.PIZZA, new VegetarianMealFactory(MealType.PIZZA),
            MealType.BURGER, new VegetarianMealFactory(MealType.BURGER),
            MealType.PASTA, new VegetarianMealFactory(MealType.PASTA));

    @Override public Meal createMeal(MealType type) {
        return meals.get(Objects.requireNonNull(type)).createMeal();
    }

    @Override public Drink createDrink() { return new Lemonade(); }
}
