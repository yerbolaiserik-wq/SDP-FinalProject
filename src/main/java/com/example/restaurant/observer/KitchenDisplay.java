package com.example.restaurant.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class KitchenDisplay implements OrderObserver {
    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

    public void onOrderChanged(OrderEvent event) {
        var order = event.order();
        String message = order.number() + " - " + order.customer() + " - " + order.status();
        if (event.previousStatus() == null) {
            StringBuilder items = new StringBuilder();
            for (var item : order.items()) {
                if (!items.isEmpty()) { items.append("; "); }
                items.append(item.quantity()).append(" x ").append(item.description());
            }
            message += " | " + items;
        }
        messages.add(message);
    }
    public List<String> messages() { return List.copyOf(messages); }
}
