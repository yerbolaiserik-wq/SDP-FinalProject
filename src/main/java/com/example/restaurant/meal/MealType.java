package com.example.restaurant.meal;

public enum MealType {
    PIZZA("Pizza"), BURGER("Burger"), PASTA("Pasta");
    private final String label;
    MealType(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
