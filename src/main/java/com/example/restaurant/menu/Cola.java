package com.example.restaurant.menu;

public final class Cola implements Drink {
    @Override public String description() { return "Cola"; }
    @Override public long priceCents() { return 200; }
}
