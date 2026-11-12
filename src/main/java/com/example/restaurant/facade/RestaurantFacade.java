package com.example.restaurant.facade;

import com.example.restaurant.meal.*;
import com.example.restaurant.menu.*;
import com.example.restaurant.decorator.*;
import com.example.restaurant.strategy.DiscountStrategy;
import com.example.restaurant.domain.*;
import com.example.restaurant.observer.*;
import com.example.restaurant.persistence.OrderRepository;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

public final class RestaurantFacade {
    private final Map<MenuFamily, MenuFactory> menus;
    private final List<DiscountStrategy> discounts;
    private final OrderRepository repository;
    private final OrderSubject events;
    private final Clock clock;
    private long nextId;

    public RestaurantFacade(Map<MenuFamily, MenuFactory> menus, List<DiscountStrategy> discounts,
                            OrderRepository repository, OrderSubject events, Clock clock) {
        this.menus = Map.copyOf(menus);
        this.discounts = List.copyOf(discounts);
        this.repository = Objects.requireNonNull(repository);
        this.events = Objects.requireNonNull(events);
        this.clock = Objects.requireNonNull(clock);
        nextId = 1;
        for (Order order : repository.all()) { nextId = Math.max(nextId, Math.addExact(order.id(), 1)); }
    }

    public Meal createMeal(MenuFamily family, MealType type, List<Topping> toppings) {
        Meal meal = menu(family).createMeal(type);
        for (Topping topping : List.copyOf(toppings)) {
            meal = switch (topping) {
                case CHEESE -> new ExtraCheese(meal);
                case MUSHROOMS -> new Mushrooms(meal);
                case SAUCE -> new ExtraSauce(meal);
            };
        }
        return meal;
    }

    public Drink createDrink(MenuFamily family) {
        return menu(family).createDrink();
    }

    private MenuFactory menu(MenuFamily family) {
        MenuFactory factory = menus.get(Objects.requireNonNull(family));
        if (factory == null) { throw new IllegalArgumentException("This menu is not available."); }
        return factory;
    }

    public List<String> discountNames() {
        List<String> names = new ArrayList<>();
        for (DiscountStrategy discount : discounts) { names.add(discount.name()); }
        return List.copyOf(names);
    }

    public long quoteDiscount(List<OrderLine> items, String discountName) {
        long subtotal = 0;
        for (OrderLine item : items) { subtotal = Math.addExact(subtotal, item.totalCents()); }
        long discount = strategy(discountName).discountCents(subtotal);
        if (discount < 0 || discount > subtotal) { throw new IllegalArgumentException("Invalid discount."); }
        return discount;
    }

    public synchronized Order placeOrder(String customer, List<OrderLine> items, String discountName) throws IOException {
        List<OrderLine> copy = List.copyOf(items);
        Order order = new Order(nextId, customer, copy, discountName, quoteDiscount(copy, discountName),
                OrderStatus.PLACED, clock.instant());
        repository.save(order);
        nextId = Math.addExact(nextId, 1);
        events.publish(new OrderEvent(order, null, clock.instant()));
        return order;
    }

    public synchronized Order changeStatus(long id, OrderStatus next) throws IOException {
        Order previous = findOrder(id);
        Order updated = previous.withStatus(next);
        repository.save(updated);
        events.publish(new OrderEvent(updated, previous.status(), clock.instant()));
        return updated;
    }

    public Order findOrder(long id) {
        return repository.find(id).orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }
    public List<Order> orders() { return repository.all(); }

    private DiscountStrategy strategy(String name) {
        for (DiscountStrategy strategy : discounts) {
            if (strategy.name().equals(name)) { return strategy; }
        }
        throw new IllegalArgumentException("Unknown discount.");
    }
}
