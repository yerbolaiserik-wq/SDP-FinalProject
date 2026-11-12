package com.example.restaurant.factory;

import com.example.restaurant.meal.*;

public final class PastaFactory extends MealFactory {
    @Override public Meal createMeal() { return new Pasta(); }
}
