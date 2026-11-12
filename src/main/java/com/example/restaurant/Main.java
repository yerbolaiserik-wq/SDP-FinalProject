package com.example.restaurant;

import com.example.restaurant.decorator.Topping;
import com.example.restaurant.domain.*;
import com.example.restaurant.facade.RestaurantFacade;
import com.example.restaurant.menu.*;
import com.example.restaurant.meal.MealType;
import com.example.restaurant.observer.*;
import com.example.restaurant.persistence.XmlOrderRepository;
import com.example.restaurant.strategy.*;
import com.example.restaurant.ui.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Map;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        boolean demo = args.length > 0 && args[0].equals("--demo");
        Path file = args.length > 1 ? Path.of(args[1]) : Path.of("data", "orders.xml");
        OrderEventSource events = new OrderEventSource();
        KitchenDisplay kitchen = new KitchenDisplay();
        CustomerDisplay customers = new CustomerDisplay();
        events.subscribe(kitchen); events.subscribe(customers);
        RestaurantFacade restaurant = new RestaurantFacade(
                Map.of(MenuFamily.REGULAR, new RegularMenuFactory(), MenuFamily.VEGETARIAN, new VegetarianMenuFactory()),
                List.of(new NoDiscount(), new StudentDiscount(), new LoyaltyDiscount()),
                new XmlOrderRepository(file), events, Clock.systemUTC());
        if (demo) {
            var pizza = restaurant.createMeal(MenuFamily.REGULAR, MealType.PIZZA, List.of(Topping.CHEESE, Topping.MUSHROOMS));
            var burger = restaurant.createMeal(MenuFamily.REGULAR, MealType.BURGER, List.of(Topping.SAUCE));
            var cola = restaurant.createDrink(MenuFamily.REGULAR);
            Order order = restaurant.placeOrder("Aigerim",
                    List.of(OrderLine.from(pizza, 2), OrderLine.from(burger, 1), OrderLine.from(cola, 1)), "Student 10%");
            System.out.println(order.number() + " | " + order.customer());
            for (OrderLine item : order.items()) {
                System.out.println(item.quantity() + " x " + item.description() + " | " + Money.format(item.totalCents()));
            }
            System.out.println("Subtotal: " + Money.format(order.subtotalCents()));
            System.out.println("Discount: " + Money.format(order.discountCents()));
            System.out.println("Total: " + Money.format(order.totalCents()));
            restaurant.changeStatus(order.id(), OrderStatus.PREPARING);
            restaurant.changeStatus(order.id(), OrderStatus.READY);
            restaurant.changeStatus(order.id(), OrderStatus.COMPLETED);
            var veggieBurger = restaurant.createMeal(MenuFamily.VEGETARIAN, MealType.BURGER, List.of());
            var lemonade = restaurant.createDrink(MenuFamily.VEGETARIAN);
            System.out.println("Vegetarian menu: " + veggieBurger.description() + " + " + lemonade.description()
                    + " | " + Money.format(veggieBurger.priceCents() + lemonade.priceCents()));
            for (String message : kitchen.messages()) { System.out.println("Kitchen: " + message); }
            for (String message : customers.messages()) { System.out.println("Customer: " + message); }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            applyTheme();
            JFrame window = new JFrame("Restaurant Ordering System");
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            window.setContentPane(new RestaurantPanel(restaurant, kitchen, customers, events));
            window.setMinimumSize(new Dimension(1100, 820));
            window.setSize(1280, 890);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    public static void applyTheme() {
        FontUIResource font = new FontUIResource("Segoe UI", Font.PLAIN, 13);
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) { UIManager.put(key, font); }
        }
        UIManager.put("Panel.background", new Color(250, 250, 251));
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(215, 234, 224));
        UIManager.put("Table.selectionForeground", new Color(22, 35, 28));
        UIManager.put("TabbedPane.selected", Color.WHITE);
    }
}
