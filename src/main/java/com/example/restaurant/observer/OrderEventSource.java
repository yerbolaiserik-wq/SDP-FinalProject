package com.example.restaurant.observer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class OrderEventSource implements OrderSubject {
    private final CopyOnWriteArrayList<OrderObserver> observers = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<OrderEvent> history = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> failures = new CopyOnWriteArrayList<>();

    public void subscribe(OrderObserver observer) { observers.addIfAbsent(Objects.requireNonNull(observer)); }
    public void unsubscribe(OrderObserver observer) { observers.remove(Objects.requireNonNull(observer)); }
    public void publish(OrderEvent event) {
        Objects.requireNonNull(event);
        history.add(event);
        for (OrderObserver observer : observers) {
            try { observer.onOrderChanged(event); }
            catch (RuntimeException error) {
                failures.add("Notification failed for " + event.order().number() + ": " + error.getMessage());
            }
        }
    }
    public List<String> failures() { return List.copyOf(failures); }
    public List<OrderEvent> history() { return List.copyOf(history); }
}
