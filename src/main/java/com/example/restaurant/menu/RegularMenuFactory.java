package com.example.restaurant.menu;

import com.example.restaurant.factory.*;
import com.example.restaurant.meal.Meal;
import com.example.restaurant.meal.MealType;
import java.util.Map;
import java.util.Objects;

public final class RegularMenuFactory implements MenuFactory {
    private final Map<MealType, MealFactory> meals = Map.of(
            MealType.PIZZA, new PizzaFactory(),
            MealType.BURGER, new BurgerFactory(),
            MealType.PASTA, new PastaFactory());

    @Override public Meal createMeal(MealType type) {
        return meals.get(Objects.requireNonNull(type)).createMeal();
    }

    @Override public Drink createDrink() { return new Cola(); }
}
