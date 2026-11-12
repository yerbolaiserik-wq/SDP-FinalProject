package com.example.restaurant.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CustomerDisplay implements OrderObserver {
    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

    public void onOrderChanged(OrderEvent event) {
        var order = event.order();
        String detail = switch (order.status()) {
            case PLACED -> "Your order has been received.";
            case PREPARING -> "The kitchen is preparing your meal.";
            case READY -> "Your order is ready for collection.";
            case COMPLETED -> "Thank you for dining with us.";
            case CANCELLED -> "Your order has been cancelled.";
        };
        messages.add(order.number() + " - " + order.customer() + ": " + detail);
    }
    public List<String> messages() { return List.copyOf(messages); }
}
