package com.example.restaurant.observer;

public interface OrderSubject {
    void subscribe(OrderObserver observer);
    void unsubscribe(OrderObserver observer);
    void publish(OrderEvent event);
}
