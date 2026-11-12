package com.example.restaurant.ui;

import com.example.restaurant.decorator.Topping;
import com.example.restaurant.domain.*;
import com.example.restaurant.facade.RestaurantFacade;
import com.example.restaurant.meal.*;
import com.example.restaurant.menu.*;
import com.example.restaurant.observer.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RestaurantPanel extends JPanel {
    private final RestaurantFacade restaurant;
    private final KitchenDisplay kitchen;
    private final CustomerDisplay customers;
    private final OrderEventSource events;
    private final List<OrderLine> basket = new ArrayList<>();
    private List<Order> orders = List.of();
    private boolean refreshingOrders;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTextField customer = new JTextField();
    private final JComboBox<MenuFamily> family = new JComboBox<>(MenuFamily.values());
    private final JComboBox<MealType> meal = new JComboBox<>(MealType.values());
    private final JCheckBox cheese = new JCheckBox("Extra cheese  +$1.50");
    private final JCheckBox mushrooms = new JCheckBox("Mushrooms  +$1.00");
    private final JCheckBox sauce = new JCheckBox("Extra sauce  +$0.50");
    private final JCheckBox drink = new JCheckBox();
    private final JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JComboBox<String> discount;
    private final JLabel unitPrice = new JLabel();
    private final JLabel totals = new JLabel();
    private final JLabel message = new JLabel("Ready");
    private final DefaultTableModel basketModel = model("Item", "Quantity", "Unit price", "Line total");
    private final JTable basketTable = new JTable(basketModel);
    private final DefaultTableModel orderModel = model("Order", "Customer", "Items", "Total", "Status");
    private final JTable orderTable = new JTable(orderModel);
    private final JTextArea receipt = textArea();
    private final JTextArea kitchenMessages = textArea();
    private final JTextArea customerMessages = textArea();
    private final JTextArea activity = textArea();
    private final JButton prepare = new JButton("Start preparing");
    private final JButton ready = new JButton("Mark ready");
    private final JButton complete = new JButton("Complete order");
    private final JButton cancel = new JButton("Cancel order");

    public RestaurantPanel(RestaurantFacade restaurant,
                           KitchenDisplay kitchen, CustomerDisplay customers, OrderEventSource events) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.kitchen = Objects.requireNonNull(kitchen);
        this.customers = Objects.requireNonNull(customers);
        this.events = Objects.requireNonNull(events);
        discount = new JComboBox<>(restaurant.discountNames().toArray(String[]::new));
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(18, 22, 12, 22));
        setBackground(new Color(247, 248, 250));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Restaurant Orders");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
        header.add(title, BorderLayout.WEST);
        JLabel business = new JLabel("MEALS  /  KITCHEN  /  COLLECTION");
        business.setForeground(new Color(90, 96, 104));
        header.add(business, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        tabs.setName("tabs");
        tabs.addTab("New order", newOrderPanel());
        tabs.addTab("Orders", ordersPanel());
        JPanel notifications = new JPanel(new GridLayout(1, 2, 16, 0));
        notifications.setBorder(new EmptyBorder(16, 16, 16, 16));
        notifications.add(labeledScroll("Kitchen updates", kitchenMessages));
        notifications.add(labeledScroll("Customer updates", customerMessages));
        tabs.addTab("Notifications", notifications);
        tabs.addTab("Event log", new JScrollPane(activity));
        add(tabs, BorderLayout.CENTER);
        message.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(message, BorderLayout.SOUTH);
        customer.setName("customer"); meal.setName("meal"); quantity.setName("quantity");
        family.setName("family"); drink.setName("drink");
        cheese.setName("cheese"); mushrooms.setName("mushrooms"); sauce.setName("sauce");
        discount.setName("discount"); orderTable.setName("orders"); basketTable.setName("basket");
        meal.addActionListener(event -> updatePrice());
        family.addActionListener(event -> updatePrice());
        cheese.addActionListener(event -> updatePrice());
        mushrooms.addActionListener(event -> updatePrice());
        sauce.addActionListener(event -> updatePrice());
        discount.addActionListener(event -> updateTotals());
        orderTable.getSelectionModel().addListSelectionListener(event -> updateSelection());
        prepare.addActionListener(event -> updateStatus(OrderStatus.PREPARING));
        ready.addActionListener(event -> updateStatus(OrderStatus.READY));
        complete.addActionListener(event -> updateStatus(OrderStatus.COMPLETED));
        cancel.addActionListener(event -> updateStatus(OrderStatus.CANCELLED));
        prepare.setName("prepare"); ready.setName("ready"); complete.setName("complete"); cancel.setName("cancel");
        updatePrice(); updateTotals(); refreshOrders(null);
    }

    private JPanel newOrderPanel() {
        JPanel result = new JPanel(new BorderLayout(20, 0));
        result.setBorder(new EmptyBorder(20, 18, 18, 18));
        JPanel form = new JPanel(new GridBagLayout());
        form.setPreferredSize(new Dimension(300, 480));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        JComponent[] fields = {new JLabel("Customer name"), customer, new JLabel("Menu family"), family,
                new JLabel("Meal"), meal, unitPrice, new JLabel("Extra toppings"), cheese, mushrooms, sauce,
                new JLabel("Matching drink"), drink, new JLabel("Quantity"), quantity,
                new JLabel("Discount"), discount};
        int row = 0;
        for (JComponent field : fields) { c.gridy = row++; form.add(field, c); }
        JButton add = new JButton("Add to basket");
        add.setName("add"); add.addActionListener(event -> addToBasket());
        c.gridy = row++; form.add(add, c);
        c.gridy = row; c.weighty = 1; form.add(new JPanel(), c);
        result.add(form, BorderLayout.WEST);
        configureTable(basketTable);
        basketTable.getColumnModel().getColumn(0).setPreferredWidth(450);
        JPanel basketPanel = new JPanel(new BorderLayout(0, 14));
        JLabel basketTitle = new JLabel("Current basket");
        basketTitle.setFont(basketTitle.getFont().deriveFont(Font.BOLD, 16f));
        basketPanel.add(basketTitle, BorderLayout.NORTH);
        basketPanel.add(tableScroll(basketTable), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 14));
        totals.setFont(totals.getFont().deriveFont(Font.BOLD, 15f));
        footer.add(totals, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton remove = new JButton("Remove item");
        remove.addActionListener(event -> removeItem());
        JButton clear = new JButton("Clear basket");
        clear.addActionListener(event -> { basket.clear(); refreshBasket(); });
        JButton place = new JButton("Place order");
        place.setName("place"); place.addActionListener(event -> placeOrder());
        buttons.add(remove); buttons.add(clear); buttons.add(place);
        footer.add(buttons, BorderLayout.SOUTH);
        basketPanel.add(footer, BorderLayout.SOUTH);
        result.add(basketPanel, BorderLayout.CENTER);
        return result;
    }

    private JPanel ordersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(prepare); actions.add(ready); actions.add(complete); actions.add(cancel);
        panel.add(actions, BorderLayout.NORTH);
        configureTable(orderTable);
        orderTable.setAutoCreateRowSorter(true);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScroll(orderTable), labeledScroll("Order receipt", receipt));
        split.setResizeWeight(0.62);
        split.setDividerLocation(330);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void addToBasket() {
        try {
            int lines = drink.isSelected() ? 2 : 1;
            if (basket.size() + lines > 50) { throw new IllegalArgumentException("The basket cannot exceed 50 lines."); }
            quantity.commitEdit();
            int count = (Integer) quantity.getValue();
            OrderLine mealLine = OrderLine.from(selectedMeal(), count);
            OrderLine drinkLine = drink.isSelected() ? OrderLine.from(restaurant.createDrink(selectedFamily()), count) : null;
            basket.add(mealLine);
            if (drinkLine != null) { basket.add(drinkLine); }
            refreshBasket();
            showMessage("Item added to basket.", false);
        } catch (java.text.ParseException error) {
            showMessage("Enter a quantity from 1 to 20.", true);
        } catch (RuntimeException error) { showMessage(error.getMessage(), true); }
    }

    private Meal selectedMeal() {
        List<Topping> toppings = new ArrayList<>();
        if (cheese.isSelected()) { toppings.add(Topping.CHEESE); }
        if (mushrooms.isSelected()) { toppings.add(Topping.MUSHROOMS); }
        if (sauce.isSelected()) { toppings.add(Topping.SAUCE); }
        return restaurant.createMeal(selectedFamily(), (MealType) meal.getSelectedItem(), toppings);
    }

    private MenuFamily selectedFamily() { return (MenuFamily) family.getSelectedItem(); }

    private void updatePrice() {
        unitPrice.setText("Meal price: " + Money.format(selectedMeal().priceCents()));
        Drink pairing = restaurant.createDrink(selectedFamily());
        drink.setText(pairing.description() + "  +" + Money.format(pairing.priceCents()));
    }

    private void refreshBasket() {
        basketModel.setRowCount(0);
        for (OrderLine item : basket) {
            basketModel.addRow(new Object[]{item.description(), item.quantity(),
                    Money.format(item.unitPriceCents()), Money.format(item.totalCents())});
        }
        updateTotals();
    }

    private void updateTotals() {
        long subtotal = 0;
        for (OrderLine item : basket) { subtotal += item.totalCents(); }
        long reduction = restaurant.quoteDiscount(basket, (String) discount.getSelectedItem());
        totals.setText("Subtotal " + Money.format(subtotal) + "    Discount -" + Money.format(reduction)
                + "    Total " + Money.format(subtotal - reduction));
    }

    private void removeItem() {
        int selected = basketTable.getSelectedRow();
        if (selected < 0) { showMessage("Select a basket item to remove.", true); return; }
        basket.remove(basketTable.convertRowIndexToModel(selected));
        refreshBasket();
    }

    private void placeOrder() {
        try {
            Order order = restaurant.placeOrder(customer.getText(), basket, (String) discount.getSelectedItem());
            basket.clear(); refreshBasket(); refreshOrders(order.id());
            tabs.setSelectedIndex(1);
            showMessage(order.number() + " placed. Kitchen and customer notified.", false);
        } catch (Exception error) { showMessage(error.getMessage(), true); }
    }

    private void updateStatus(OrderStatus next) {
        Order selected = selectedOrder();
        if (selected == null) { showMessage("Select an order first.", true); return; }
        try {
            Order order = restaurant.changeStatus(selected.id(), next);
            refreshOrders(order.id());
            showMessage(order.number() + " is now " + order.status().toString().toLowerCase() + ".", false);
        } catch (Exception error) { showMessage(error.getMessage(), true); }
    }

    private void refreshOrders(Long selectedId) {
        refreshingOrders = true;
        List<? extends RowSorter.SortKey> sortKeys = orderTable.getRowSorter() == null
                ? List.of() : orderTable.getRowSorter().getSortKeys();
        orderTable.setRowSorter(null);
        orderTable.clearSelection();
        orders = restaurant.orders();
        orderModel.setRowCount(0);
        for (Order order : orders) {
            int count = 0;
            for (OrderLine item : order.items()) { count += item.quantity(); }
            orderModel.addRow(new Object[]{order.number(), order.customer(), count, Money.format(order.totalCents()), order.status()});
        }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(orderModel);
        sorter.setComparator(2, (first, second) -> Integer.compare((Integer) first, (Integer) second));
        sorter.setComparator(3, (first, second) -> Long.compare(parseMoney(first), parseMoney(second)));
        sorter.setSortKeys(sortKeys);
        orderTable.setRowSorter(sorter);
        for (int index = 0; index < orders.size(); index++) {
            if (selectedId != null && orders.get(index).id() == selectedId) {
                int row = orderTable.convertRowIndexToView(index);
                orderTable.setRowSelectionInterval(row, row);
            }
        }
        kitchenMessages.setText(String.join("\n\n", kitchen.messages()));
        customerMessages.setText(String.join("\n\n", customers.messages()));
        StringBuilder eventLog = new StringBuilder();
        for (OrderEvent event : events.history()) {
            eventLog.append(event.order().number()).append(" | ").append(event.order().status()).append('\n');
        }
        activity.setText(eventLog.toString());
        if (!events.failures().isEmpty()) { activity.append("\n\n" + String.join("\n", events.failures())); }
        refreshingOrders = false;
        updateSelection();
    }

    private Order selectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { return null; }
        int index = orderTable.convertRowIndexToModel(row);
        return index < orders.size() ? orders.get(index) : null;
    }

    private void updateSelection() {
        if (refreshingOrders) { return; }
        Order order = selectedOrder();
        prepare.setEnabled(order != null && order.status().canMoveTo(OrderStatus.PREPARING));
        ready.setEnabled(order != null && order.status().canMoveTo(OrderStatus.READY));
        complete.setEnabled(order != null && order.status().canMoveTo(OrderStatus.COMPLETED));
        cancel.setEnabled(order != null && order.status().canMoveTo(OrderStatus.CANCELLED));
        if (order == null) { receipt.setText(""); return; }
        StringBuilder text = new StringBuilder(order.number() + "  |  " + order.customer() + "  |  " + order.status() + "\n\n");
        for (OrderLine item : order.items()) {
            text.append(item.quantity()).append(" x ").append(item.description())
                    .append("    ").append(Money.format(item.totalCents())).append('\n');
        }
        text.append("\nSubtotal: ").append(Money.format(order.subtotalCents()))
                .append("    ").append(order.discountName()).append(": -").append(Money.format(order.discountCents()))
                .append("    Total: ").append(Money.format(order.totalCents()));
        receipt.setText(text.toString()); receipt.setCaretPosition(0);
    }

    private void showMessage(String text, boolean error) {
        message.setForeground(error ? new Color(166, 35, 35) : new Color(31, 100, 69));
        message.setText(text == null ? "The operation could not be completed." : text);
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    private static long parseMoney(Object value) {
        String amount = value.toString().replace("$", "").replace(",", "");
        return new java.math.BigDecimal(amount).movePointRight(2).longValueExact();
    }
    private static void configureTable(JTable table) {
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(225, 228, 233));
        table.getTableHeader().setReorderingAllowed(false);
    }
    private static JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setMargin(new Insets(12, 12, 12, 12));
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return area;
    }
    private static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        return scroll;
    }
    private static JPanel labeledScroll(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }
}
