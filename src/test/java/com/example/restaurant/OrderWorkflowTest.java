package com.example.restaurant;

import com.example.restaurant.decorator.Topping;
import com.example.restaurant.domain.*;
import com.example.restaurant.meal.MealType;
import com.example.restaurant.menu.MenuFamily;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderWorkflowTest {
    @TempDir Path directory;

    @Test void sixPatternsCooperateInCompleteOrder() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        var pizza = app.restaurant.createMeal(MenuFamily.REGULAR, MealType.PIZZA, List.of(Topping.CHEESE, Topping.MUSHROOMS));
        var burger = app.restaurant.createMeal(MenuFamily.REGULAR, MealType.BURGER, List.of(Topping.SAUCE));
        var cola = app.restaurant.createDrink(MenuFamily.REGULAR);
        Order order = app.restaurant.placeOrder("Aigerim",
                List.of(OrderLine.from(pizza, 2), OrderLine.from(burger, 1), OrderLine.from(cola, 1)), "Student 10%");
        assertEquals(4050, order.subtotalCents());
        assertEquals(405, order.discountCents());
        assertEquals(3645, order.totalCents());
        assertEquals(OrderStatus.PLACED, order.status());
        app.restaurant.changeStatus(order.id(), OrderStatus.PREPARING);
        app.restaurant.changeStatus(order.id(), OrderStatus.READY);
        app.restaurant.changeStatus(order.id(), OrderStatus.COMPLETED);
        assertEquals(OrderStatus.COMPLETED, app.repository.find(order.id()).orElseThrow().status());
        assertEquals(4, app.kitchen.messages().size());
        assertEquals(4, app.customers.messages().size());
        assertEquals(4, app.events.history().size());
        assertTrue(app.customers.messages().get(2).contains("ready for collection"));
    }
    @Test void placedOrderCanBeCancelled() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        Order order = app.place();
        assertEquals(OrderStatus.CANCELLED, app.restaurant.changeStatus(order.id(), OrderStatus.CANCELLED).status());
        assertTrue(app.customers.messages().get(1).contains("cancelled"));
    }
    @Test void preparingOrderCanBeCancelled() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory); Order order = app.place();
        app.restaurant.changeStatus(order.id(), OrderStatus.PREPARING);
        assertEquals(OrderStatus.CANCELLED, app.restaurant.changeStatus(order.id(), OrderStatus.CANCELLED).status());
    }
    @Test void cannotSkipPreparation() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory); Order order = app.place();
        assertThrows(IllegalStateException.class, () -> app.restaurant.changeStatus(order.id(), OrderStatus.READY));
        assertEquals(OrderStatus.PLACED, app.restaurant.findOrder(order.id()).status());
        assertEquals(1, app.kitchen.messages().size());
    }
    @Test void completedOrderIsTerminal() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory); Order order = app.place();
        app.restaurant.changeStatus(order.id(), OrderStatus.PREPARING);
        app.restaurant.changeStatus(order.id(), OrderStatus.READY);
        app.restaurant.changeStatus(order.id(), OrderStatus.COMPLETED);
        assertThrows(IllegalStateException.class, () -> app.restaurant.changeStatus(order.id(), OrderStatus.CANCELLED));
    }
    @Test void cancelledOrderIsTerminal() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory); Order order = app.place();
        app.restaurant.changeStatus(order.id(), OrderStatus.CANCELLED);
        assertThrows(IllegalStateException.class, () -> app.restaurant.changeStatus(order.id(), OrderStatus.PREPARING));
    }
    @Test void readyOrderCannotBeCancelled() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory); Order order = app.place();
        app.restaurant.changeStatus(order.id(), OrderStatus.PREPARING);
        app.restaurant.changeStatus(order.id(), OrderStatus.READY);
        assertThrows(IllegalStateException.class, () -> app.restaurant.changeStatus(order.id(), OrderStatus.CANCELLED));
    }
    @Test void blankCustomerIsRejectedWithoutSaving() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        assertThrows(IllegalArgumentException.class, () -> app.restaurant.placeOrder(" ", List.of(new OrderLine("Pizza", 1, 1200)), "No discount"));
        assertTrue(app.repository.all().isEmpty());
        assertTrue(app.kitchen.messages().isEmpty());
    }
    @Test void emptyBasketIsRejected() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        assertThrows(IllegalArgumentException.class, () -> app.restaurant.placeOrder("Customer", List.of(), "No discount"));
    }
    @Test void unknownDiscountIsRejected() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        assertThrows(IllegalArgumentException.class, () -> app.restaurant.placeOrder("Customer", List.of(new OrderLine("Pizza", 1, 1200)), "missing"));
    }
    @Test void unknownOrderIsRejected() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        assertThrows(IllegalArgumentException.class, () -> app.restaurant.changeStatus(99, OrderStatus.PREPARING));
    }
    @Test void orderKeepsImmutableBasketSnapshot() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        List<OrderLine> basket = new ArrayList<>(List.of(new OrderLine("Pizza", 1, 1200)));
        Order order = app.restaurant.placeOrder("Customer", basket, "No discount");
        basket.clear();
        assertEquals(1, order.items().size());
        assertThrows(UnsupportedOperationException.class, () -> order.items().clear());
    }
    @Test void failedPlacementDoesNotPublishAnEvent() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        assertThrows(IllegalArgumentException.class, () -> app.restaurant.placeOrder("", List.of(), "No discount"));
        assertTrue(app.events.history().isEmpty());
    }
    @Test void restartingRestoresOrdersAndAdvancesNumber() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        Order first = app.place();
        app.restaurant.changeStatus(first.id(), OrderStatus.PREPARING);
        RestaurantFixture restarted = new RestaurantFixture(directory);
        assertEquals(OrderStatus.PREPARING, restarted.restaurant.findOrder(first.id()).status());
        assertEquals(first.id() + 1, restarted.place().id());
        assertEquals(1, restarted.kitchen.messages().size());
    }
    @Test void quoteMatchesPlacedOrder() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        List<OrderLine> lines = List.of(new OrderLine("Pasta", 1, 1250));
        long discount = app.restaurant.quoteDiscount(lines, "Loyalty 15%");
        assertEquals(187, discount);
        assertEquals(discount, app.restaurant.placeOrder("Daniel", lines, "Loyalty 15%").discountCents());
    }
}
