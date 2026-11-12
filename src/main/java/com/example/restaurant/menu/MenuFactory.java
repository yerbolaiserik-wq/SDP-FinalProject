package com.example.restaurant.menu;

import com.example.restaurant.meal.Meal;
import com.example.restaurant.meal.MealType;

public interface MenuFactory {
    Meal createMeal(MealType type);
    Drink createDrink();
}
