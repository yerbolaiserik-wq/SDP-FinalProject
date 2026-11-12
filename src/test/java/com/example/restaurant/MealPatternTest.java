package com.example.restaurant;

import com.example.restaurant.meal.*;
import com.example.restaurant.factory.*;
import com.example.restaurant.decorator.*;
import com.example.restaurant.strategy.*;
import com.example.restaurant.domain.*;
import com.example.restaurant.ui.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MealPatternTest {
    @Test void pizzaFactoryCreatesPizza() { assertInstanceOf(Pizza.class, new PizzaFactory().createMeal()); }
    @Test void burgerFactoryCreatesBurger() { assertInstanceOf(Burger.class, new BurgerFactory().createMeal()); }
    @Test void pastaFactoryCreatesPasta() { assertInstanceOf(Pasta.class, new PastaFactory().createMeal()); }
    @Test void factoryCreatesIndependentObjects() {
        MealFactory factory = new PizzaFactory();
        assertNotSame(factory.createMeal(), factory.createMeal());
    }
    @Test void allBasePricesAreCorrect() {
        assertEquals(1200, new Pizza().priceCents());
        assertEquals(900, new Burger().priceCents());
        assertEquals(1100, new Pasta().priceCents());
    }
    @Test void decoratorChangesPriceAndDescription() {
        Meal meal = new ExtraCheese(new Pizza());
        assertEquals(1350, meal.priceCents());
        assertEquals("Margherita pizza + extra cheese", meal.description());
    }
    @Test void decoratorsComposeAndLeaveBaseUnchanged() {
        Meal base = new Pizza();
        Meal decorated = new ExtraSauce(new Mushrooms(new ExtraCheese(base)));
        assertEquals(1500, decorated.priceCents());
        assertEquals(1200, base.priceCents());
        assertEquals("Margherita pizza + extra cheese + mushrooms + extra sauce", decorated.description());
    }
    @Test void sameDecoratorCanWrapTwice() { assertEquals(1500, new ExtraCheese(new ExtraCheese(new Pizza())).priceCents()); }
    @Test void decoratorRejectsNullMeal() { assertThrows(NullPointerException.class, () -> new ExtraCheese(null)); }
    @Test void strategiesProduceDifferentPrices() {
        assertEquals(0, new NoDiscount().discountCents(3850));
        assertEquals(385, new StudentDiscount().discountCents(3850));
        assertEquals(577, new LoyaltyDiscount().discountCents(3850));
    }
    @Test void discountFractionIsRoundedDownToWholeCents() { assertEquals(1, new LoyaltyDiscount().discountCents(13)); }
    @Test void discountRejectsNegativeSubtotal() {
        assertThrows(IllegalArgumentException.class, () -> new NoDiscount().discountCents(-1));
        assertThrows(IllegalArgumentException.class, () -> new StudentDiscount().discountCents(-1));
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyDiscount().discountCents(-1));
    }
    @Test void lineMultipliesQuantityAndPrice() { assertEquals(2700, new OrderLine("Burger", 3, 900).totalCents()); }
    @Test void lineRejectsInvalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new OrderLine("Pizza", 0, 1200));
        assertThrows(IllegalArgumentException.class, () -> new OrderLine("Pizza", 21, 1200));
    }
    @Test void lineRejectsInvalidDescriptionAndPrice() {
        assertThrows(IllegalArgumentException.class, () -> new OrderLine(" ", 1, 100));
        assertThrows(IllegalArgumentException.class, () -> new OrderLine("Pizza", 1, -1));
    }
    @Test void moneyFormatsIntegerCents() { assertEquals("$34.65", Money.format(3465)); }
}
