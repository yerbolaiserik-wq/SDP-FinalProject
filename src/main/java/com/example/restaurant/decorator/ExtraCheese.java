package com.example.restaurant.decorator;

import com.example.restaurant.meal.Meal;

public final class ExtraCheese extends MealDecorator {
    public ExtraCheese(Meal meal) { super(meal); }
    public String description() { return meal.description() + " + extra cheese"; }
    public long priceCents() { return Math.addExact(meal.priceCents(), 150); }
}
