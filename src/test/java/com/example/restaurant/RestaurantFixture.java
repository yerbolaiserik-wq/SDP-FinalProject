package com.example.restaurant;

import com.example.restaurant.domain.*;
import com.example.restaurant.facade.RestaurantFacade;
import com.example.restaurant.menu.*;
import com.example.restaurant.observer.*;
import com.example.restaurant.persistence.XmlOrderRepository;
import com.example.restaurant.strategy.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.*;
import java.util.*;

final class RestaurantFixture {
    final OrderEventSource events = new OrderEventSource();
    final KitchenDisplay kitchen = new KitchenDisplay();
    final CustomerDisplay customers = new CustomerDisplay();
    final XmlOrderRepository repository;
    final RestaurantFacade restaurant;

    RestaurantFixture(Path directory) throws IOException {
        repository = new XmlOrderRepository(directory.resolve("orders.xml"));
        events.subscribe(kitchen); events.subscribe(customers);
        restaurant = new RestaurantFacade(
                Map.of(MenuFamily.REGULAR, new RegularMenuFactory(), MenuFamily.VEGETARIAN, new VegetarianMenuFactory()),
                List.of(new NoDiscount(), new StudentDiscount(), new LoyaltyDiscount()),
                repository, events, Clock.fixed(Instant.parse("2026-11-08T12:00:00Z"), ZoneOffset.UTC));
    }

    Order place() throws IOException {
        return restaurant.placeOrder("Aigerim", List.of(new OrderLine("Pizza", 2, 1200)), "Student 10%");
    }
}
