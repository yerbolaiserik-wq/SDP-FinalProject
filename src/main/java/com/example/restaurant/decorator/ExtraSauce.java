package com.example.restaurant.decorator;

import com.example.restaurant.meal.Meal;

public final class ExtraSauce extends MealDecorator {
    public ExtraSauce(Meal meal) { super(meal); }
    public String description() { return meal.description() + " + extra sauce"; }
    public long priceCents() { return Math.addExact(meal.priceCents(), 50); }
}
