package com.example.restaurant.observer;

@FunctionalInterface
public interface OrderObserver {
    void onOrderChanged(OrderEvent event);
}
