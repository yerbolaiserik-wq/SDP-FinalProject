package com.example.restaurant;

import com.example.restaurant.meal.MealType;
import com.example.restaurant.menu.MenuFamily;
import com.example.restaurant.domain.OrderStatus;
import com.example.restaurant.ui.RestaurantPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationViewTest {
    @TempDir Path directory;

    private Component find(Container container, String name) {
        for (Component child : container.getComponents()) {
            if (name.equals(child.getName())) { return child; }
            if (child instanceof Container nested) {
                Component result = find(nested, name);
                if (result != null) { return result; }
            }
        }
        return null;
    }
    private void button(RestaurantPanel panel, String name) { ((JButton) find(panel, name)).doClick(); }
    private void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) { if (child instanceof Container nested) { layout(nested); } }
    }
    private void capture(RestaurantPanel panel, String name) {
        panel.setSize(1280, 820); layout(panel);
        BufferedImage image = new BufferedImage(2560, 1640, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.scale(2, 2);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        panel.printAll(graphics); graphics.dispose();
        try {
            Path output = Path.of("target", "screenshots", name + ".png");
            Files.createDirectories(output.getParent());
            ImageIO.write(image, "png", output.toFile());
        } catch (IOException error) { throw new IllegalStateException(error); }
    }

    @Test void desktopCheckoutAndStatusButtonsExecuteRealWorkflow() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        SwingUtilities.invokeAndWait(() -> {
            Main.applyTheme();
            RestaurantPanel panel = new RestaurantPanel(app.restaurant, app.kitchen, app.customers, app.events);
            ((JTextField) find(panel, "customer")).setText("Aigerim");
            ((JCheckBox) find(panel, "cheese")).doClick();
            ((JCheckBox) find(panel, "mushrooms")).doClick();
            ((JSpinner) find(panel, "quantity")).setValue(2);
            button(panel, "add");
            ((JComboBox<?>) find(panel, "meal")).setSelectedItem(MealType.BURGER);
            ((JCheckBox) find(panel, "cheese")).doClick();
            ((JCheckBox) find(panel, "mushrooms")).doClick();
            ((JCheckBox) find(panel, "sauce")).doClick();
            ((JCheckBox) find(panel, "drink")).doClick();
            ((JSpinner) find(panel, "quantity")).setValue(1);
            button(panel, "add");
            ((JComboBox<?>) find(panel, "discount")).setSelectedItem("Student 10%");
            capture(panel, "order-entry");
            button(panel, "place");
            assertEquals(3645, app.restaurant.orders().get(0).totalCents());
            assertFalse(((JButton) find(panel, "ready")).isEnabled());
            button(panel, "prepare"); button(panel, "ready");
            assertEquals(OrderStatus.READY, app.restaurant.orders().get(0).status());
            capture(panel, "order-tracking");
            ((JTabbedPane) find(panel, "tabs")).setSelectedIndex(2);
            capture(panel, "notifications");
            assertEquals(3, app.kitchen.messages().size());
            button(panel, "complete");
            assertEquals(OrderStatus.COMPLETED, app.restaurant.orders().get(0).status());
        });
    }

    @Test void invalidCheckoutKeepsBasketAndDoesNotCreateOrder() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        SwingUtilities.invokeAndWait(() -> {
            Main.applyTheme();
            RestaurantPanel panel = new RestaurantPanel(app.restaurant, app.kitchen, app.customers, app.events);
            button(panel, "add"); button(panel, "place");
            assertTrue(app.restaurant.orders().isEmpty());
            assertEquals(1, ((JTable) find(panel, "basket")).getRowCount());
        });
    }

    @Test void sortedSelectionUpdatesTheSelectedOrderOnly() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        var first = app.place();
        var second = app.restaurant.placeOrder("Zara", first.items(), "No discount");
        SwingUtilities.invokeAndWait(() -> {
            RestaurantPanel panel = new RestaurantPanel(app.restaurant, app.kitchen, app.customers, app.events);
            JTable table = (JTable) find(panel, "orders");
            table.getRowSorter().toggleSortOrder(1);
            table.getRowSorter().toggleSortOrder(1);
            table.setRowSelectionInterval(0, 0);
            button(panel, "prepare");
            assertEquals(OrderStatus.PREPARING, app.restaurant.findOrder(second.id()).status());
            assertEquals(OrderStatus.PLACED, app.restaurant.findOrder(first.id()).status());
        });
    }

    @Test void vegetarianFamilyAddsMatchingMealAndDrink() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        SwingUtilities.invokeAndWait(() -> {
            Main.applyTheme();
            RestaurantPanel panel = new RestaurantPanel(app.restaurant, app.kitchen, app.customers, app.events);
            ((JTextField) find(panel, "customer")).setText("Daniel");
            ((JComboBox<?>) find(panel, "family")).setSelectedItem(MenuFamily.VEGETARIAN);
            ((JComboBox<?>) find(panel, "meal")).setSelectedItem(MealType.BURGER);
            JCheckBox drink = (JCheckBox) find(panel, "drink");
            assertTrue(drink.getText().startsWith("Fresh lemonade"));
            drink.doClick();
            button(panel, "add");
            capture(panel, "vegetarian-menu");
            button(panel, "place");
            var order = app.restaurant.orders().get(0);
            assertEquals(1200, order.totalCents());
            assertEquals("Veggie burger", order.items().get(0).description());
            assertEquals("Fresh lemonade", order.items().get(1).description());
        });
    }
}
