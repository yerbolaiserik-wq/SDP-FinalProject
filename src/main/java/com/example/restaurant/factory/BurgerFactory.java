package com.example.restaurant.factory;

import com.example.restaurant.meal.*;

public final class BurgerFactory extends MealFactory {
    @Override public Meal createMeal() { return new Burger(); }
}
