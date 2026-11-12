package com.example.restaurant.decorator;

import com.example.restaurant.meal.Meal;

public final class Mushrooms extends MealDecorator {
    public Mushrooms(Meal meal) { super(meal); }
    public String description() { return meal.description() + " + mushrooms"; }
    public long priceCents() { return Math.addExact(meal.priceCents(), 100); }
}
