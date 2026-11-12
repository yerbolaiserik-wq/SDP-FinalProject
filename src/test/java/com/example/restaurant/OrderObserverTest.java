package com.example.restaurant;

import com.example.restaurant.domain.*;
import com.example.restaurant.observer.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class OrderObserverTest {
    private OrderEvent event() {
        return new OrderEvent(new Order(1, "Customer", List.of(new OrderLine("Pizza", 1, 1200)),
                "No discount", 0, OrderStatus.PLACED, Instant.EPOCH), null, Instant.EPOCH);
    }
    @Test void duplicateSubscriptionNotifiesOnce() {
        OrderEventSource source = new OrderEventSource(); AtomicInteger count = new AtomicInteger();
        OrderObserver listener = update -> count.incrementAndGet();
        source.subscribe(listener); source.subscribe(listener); source.publish(event());
        assertEquals(1, count.get());
    }
    @Test void unsubscribeStopsLaterEvents() {
        OrderEventSource source = new OrderEventSource(); AtomicInteger count = new AtomicInteger();
        OrderObserver listener = update -> count.incrementAndGet();
        source.subscribe(listener); source.unsubscribe(listener); source.publish(event());
        assertEquals(0, count.get());
    }
    @Test void observerCanLeaveInsideCallback() {
        OrderEventSource source = new OrderEventSource(); AtomicInteger count = new AtomicInteger();
        OrderObserver listener = new OrderObserver() {
            public void onOrderChanged(OrderEvent update) { count.incrementAndGet(); source.unsubscribe(this); }
        };
        source.subscribe(listener); source.publish(event()); source.publish(event());
        assertEquals(1, count.get());
    }
    @Test void listenerFailureDoesNotPreventCustomerNotification() {
        OrderEventSource source = new OrderEventSource(); CustomerDisplay customer = new CustomerDisplay();
        source.subscribe(update -> { throw new IllegalStateException("offline"); });
        source.subscribe(customer);
        assertDoesNotThrow(() -> source.publish(event()));
        assertEquals(1, customer.messages().size());
        assertEquals(1, source.failures().size());
    }
}
