package com.example.restaurant.persistence;

import com.example.restaurant.domain.*;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class XmlOrderRepository implements OrderRepository {
    private final Path file;
    private Map<Long, Order> orders;

    public XmlOrderRepository(Path file) throws IOException {
        this.file = file.toAbsolutePath();
        this.orders = load();
    }

    public synchronized List<Order> all() { return List.copyOf(orders.values()); }
    public synchronized Optional<Order> find(long id) { return Optional.ofNullable(orders.get(id)); }

    public synchronized void save(Order order) throws IOException {
        Objects.requireNonNull(order);
        Map<Long, Order> updated = new TreeMap<>(orders);
        updated.put(order.id(), order);
        Properties data = new Properties();
        data.setProperty("count", Integer.toString(updated.size()));
        int index = 0;
        for (Order entry : updated.values()) {
            String prefix = "order." + index++ + ".";
            data.setProperty(prefix + "id", Long.toString(entry.id()));
            data.setProperty(prefix + "customer", entry.customer());
            data.setProperty(prefix + "discountName", entry.discountName());
            data.setProperty(prefix + "discountCents", Long.toString(entry.discountCents()));
            data.setProperty(prefix + "status", entry.status().name());
            data.setProperty(prefix + "createdAt", entry.createdAt().toString());
            data.setProperty(prefix + "items", Integer.toString(entry.items().size()));
            for (int i = 0; i < entry.items().size(); i++) {
                OrderLine item = entry.items().get(i);
                String key = prefix + "item." + i + ".";
                data.setProperty(key + "description", item.description());
                data.setProperty(key + "quantity", Integer.toString(item.quantity()));
                data.setProperty(key + "price", Long.toString(item.unitPriceCents()));
            }
        }
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "orders-", ".xml");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                data.storeToXML(output, "Restaurant order history", "UTF-8");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            orders = updated;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Map<Long, Order> load() throws IOException {
        Map<Long, Order> loaded = new TreeMap<>();
        if (!Files.exists(file)) { return loaded; }
        Properties data = new Properties();
        try (InputStream input = Files.newInputStream(file)) { data.loadFromXML(input); }
        try {
            int count = Integer.parseInt(required(data, "count"));
            if (count < 0 || count > 100000) { throw new IllegalArgumentException("Invalid order count."); }
            for (int index = 0; index < count; index++) {
                String prefix = "order." + index + ".";
                int itemCount = Integer.parseInt(required(data, prefix + "items"));
                if (itemCount < 1 || itemCount > 50) { throw new IllegalArgumentException("Invalid item count."); }
                List<OrderLine> items = new ArrayList<>();
                for (int i = 0; i < itemCount; i++) {
                    String key = prefix + "item." + i + ".";
                    items.add(new OrderLine(required(data, key + "description"),
                            Integer.parseInt(required(data, key + "quantity")),
                            Long.parseLong(required(data, key + "price"))));
                }
                Order order = new Order(Long.parseLong(required(data, prefix + "id")),
                        required(data, prefix + "customer"), items, required(data, prefix + "discountName"),
                        Long.parseLong(required(data, prefix + "discountCents")),
                        OrderStatus.valueOf(required(data, prefix + "status")),
                        Instant.parse(required(data, prefix + "createdAt")));
                if (loaded.put(order.id(), order) != null) { throw new IllegalArgumentException("Duplicate order number."); }
            }
        } catch (RuntimeException error) {
            throw new IOException("Order history is invalid: " + file.getFileName(), error);
        }
        return loaded;
    }

    private String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null) { throw new IllegalArgumentException("Missing order field: " + key); }
        return value;
    }
}
