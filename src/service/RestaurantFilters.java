package service;

import models.Restaurant;

import java.math.BigDecimal;
import java.util.function.Predicate;

public final class RestaurantFilters {
    private RestaurantFilters() {}

    public static Predicate<Restaurant> byDistrict(String district) {
        return r -> r.getDistrict().equalsIgnoreCase(district);
    }

    public static Predicate<Restaurant> byCuisine(String cuisine) {
        return r -> r.getCuisines().stream().anyMatch(c -> c.equalsIgnoreCase(cuisine));
    }

    public static Predicate<Restaurant> minRating(double rating) {
        return r -> r.getAverageRating() != null && r.getAverageRating() >= rating;
    }

    public static Predicate<Restaurant> maxPrice(BigDecimal ceiling) {
        return r -> r.getMenuItems().values()
                .stream()
                .anyMatch(item -> item.getPrice().compareTo(ceiling) <= 0);
    }

    public static Predicate<Restaurant> open() {
        return Restaurant::isOpen;
    }
}
