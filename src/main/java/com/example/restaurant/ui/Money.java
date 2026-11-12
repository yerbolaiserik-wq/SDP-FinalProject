package com.example.restaurant.ui;

import java.math.BigDecimal;
import java.util.Locale;

public final class Money {
    private Money() {}
    public static String format(long cents) {
        return String.format(Locale.US, "$%,.2f", BigDecimal.valueOf(cents, 2));
    }
}
