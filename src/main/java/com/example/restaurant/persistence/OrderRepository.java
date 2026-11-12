package com.example.restaurant.persistence;

import com.example.restaurant.domain.Order;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> all();
    Optional<Order> find(long id);
    void save(Order order) throws IOException;
}
