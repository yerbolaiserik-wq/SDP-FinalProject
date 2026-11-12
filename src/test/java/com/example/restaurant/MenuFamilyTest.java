package com.example.restaurant;

import com.example.restaurant.decorator.Topping;
import com.example.restaurant.domain.OrderLine;
import com.example.restaurant.facade.RestaurantFacade;
import com.example.restaurant.meal.*;
import com.example.restaurant.menu.*;
import com.example.restaurant.strategy.NoDiscount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MenuFamilyTest {
    @TempDir Path directory;

    @Test void regularFamilyCreatesRegularMealsAndCola() {
        MenuFactory menu = new RegularMenuFactory();
        assertInstanceOf(Pizza.class, menu.createMeal(MealType.PIZZA));
        assertInstanceOf(Burger.class, menu.createMeal(MealType.BURGER));
        assertInstanceOf(Pasta.class, menu.createMeal(MealType.PASTA));
        assertInstanceOf(Cola.class, menu.createDrink());
        assertEquals(200, menu.createDrink().priceCents());
    }

    @Test void vegetarianFamilyCreatesVegetarianMealsAndLemonade() {
        MenuFactory menu = new VegetarianMenuFactory();
        for (MealType type : MealType.values()) {
            assertEquals(type, assertInstanceOf(VegetarianMeal.class, menu.createMeal(type)).type());
        }
        assertInstanceOf(Lemonade.class, menu.createDrink());
        assertEquals(250, menu.createDrink().priceCents());
    }

    @Test void vegetarianCreatorReturnsIndependentMeals() {
        MenuFactory menu = new VegetarianMenuFactory();
        assertNotSame(menu.createMeal(MealType.PASTA), menu.createMeal(MealType.PASTA));
        assertEquals(1150, menu.createMeal(MealType.PASTA).priceCents());
    }

    @Test void facadeAppliesDecoratorsToVegetarianMeals() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        Meal meal = app.restaurant.createMeal(MenuFamily.VEGETARIAN, MealType.BURGER, List.of(Topping.CHEESE, Topping.SAUCE));
        assertEquals("Veggie burger + extra cheese + extra sauce", meal.description());
        assertEquals(1150, meal.priceCents());
    }

    @Test void matchingProductsSurviveRestartAndReachKitchen() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        var meal = app.restaurant.createMeal(MenuFamily.VEGETARIAN, MealType.BURGER, List.of());
        var drink = app.restaurant.createDrink(MenuFamily.VEGETARIAN);
        var order = app.restaurant.placeOrder("Daniel", List.of(OrderLine.from(meal, 2), OrderLine.from(drink, 2)), "No discount");
        assertEquals(2400, order.totalCents());
        assertTrue(app.kitchen.messages().get(0).contains("2 x Fresh lemonade"));
        assertEquals(order, new RestaurantFixture(directory).restaurant.findOrder(order.id()));
    }

    @Test void unavailableFamilyIsRejected() throws Exception {
        RestaurantFixture app = new RestaurantFixture(directory);
        var restricted = new RestaurantFacade(Map.of(MenuFamily.REGULAR, new RegularMenuFactory()),
                List.of(new NoDiscount()), app.repository, app.events, Clock.systemUTC());
        assertThrows(IllegalArgumentException.class, () -> restricted.createDrink(MenuFamily.VEGETARIAN));
    }
}
