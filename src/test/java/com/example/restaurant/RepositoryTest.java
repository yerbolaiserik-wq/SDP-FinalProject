package com.example.restaurant;

import com.example.restaurant.domain.*;
import com.example.restaurant.persistence.XmlOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    @TempDir Path directory;
    private Order sample() {
        return new Order(1, "Aigerim & Family", List.of(new OrderLine("Pizza <large> & cheese", 2, 1350)),
                "Student 10%", 270, OrderStatus.PLACED, Instant.parse("2026-11-08T12:00:00Z"));
    }
    @Test void xmlRoundTripPreservesSpecialCharactersAndAmounts() throws Exception {
        Path file = directory.resolve("orders.xml");
        new XmlOrderRepository(file).save(sample());
        assertEquals(sample(), new XmlOrderRepository(file).find(1).orElseThrow());
    }
    @Test void missingHistoryIsEmpty() throws Exception {
        assertTrue(new XmlOrderRepository(directory.resolve("orders.xml")).all().isEmpty());
    }
    @Test void corruptedHistoryIsNotSilentlyReset() throws Exception {
        Path file = directory.resolve("orders.xml");
        Files.writeString(file, "not XML");
        assertThrows(IOException.class, () -> new XmlOrderRepository(file));
        assertEquals("not XML", Files.readString(file));
    }
    @Test void failedWriteDoesNotChangeInMemoryOrders() throws Exception {
        Path notDirectory = directory.resolve("file");
        Files.writeString(notDirectory, "occupied");
        XmlOrderRepository repository = new XmlOrderRepository(notDirectory.resolve("orders.xml"));
        assertThrows(IOException.class, () -> repository.save(sample()));
        assertTrue(repository.all().isEmpty());
    }
}
