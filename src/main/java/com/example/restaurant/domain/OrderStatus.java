package com.example.restaurant.domain;

public enum OrderStatus {
    PLACED("Placed"), PREPARING("Preparing"), READY("Ready"), COMPLETED("Completed"), CANCELLED("Cancelled");
    private final String label;
    OrderStatus(String label) { this.label = label; }

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PLACED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == READY || next == CANCELLED;
            case READY -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
    @Override public String toString() { return label; }
}
