package com.example.restaurant.menu;

public final class Lemonade implements Drink {
    @Override public String description() { return "Fresh lemonade"; }
    @Override public long priceCents() { return 250; }
}
