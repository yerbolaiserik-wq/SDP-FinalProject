package com.example.restaurant.menu;

public enum MenuFamily {
    REGULAR("Regular"), VEGETARIAN("Vegetarian");

    private final String label;

    MenuFamily(String label) { this.label = label; }

    @Override public String toString() { return label; }
}
