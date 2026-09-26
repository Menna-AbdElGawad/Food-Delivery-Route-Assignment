package models;

import enums.OrderStatus;
import enums.RestaurantStatus;
import exceptions.IllegalOrderStatusTransitionException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Getter
public class Restaurant {
    private static int nextId = 1;
    private int id;
    private String displayName;
    private String district;
    private Set<String> cuisines = new HashSet<>(); // 1 or more
    private Double averageRating; // 0.0 -> 5.0
    private BigDecimal revenue;
    private RestaurantStatus status;
    private final Map<Integer, MenuItem> menuItems = new LinkedHashMap<>();

    public Restaurant(String displayName, String districts, Collection<String> cuisines,
                      Double averageRating, RestaurantStatus status) {

        if(displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if(districts == null || districts.isBlank()) {
            throw new IllegalArgumentException("District is required.");

        }

        if(cuisines == null || cuisines.isEmpty()) {
            throw new IllegalArgumentException("At least one cuisine is required.");
        }

        if(status == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        this.id = nextId++;
        this.displayName = displayName;
        this.district = districts;
        this.cuisines.addAll(cuisines);
        this.averageRating = averageRating;
        this.status = status;
    }

    // addMenuItem
    public void addMenuItem(MenuItem menuItem) {
        if(menuItem.getRestaurantId() != id) {
            throw new IllegalArgumentException("Item belongs to another restaurant.");
        }

        if(menuItems.containsKey(menuItem.getId())) {
            throw new IllegalArgumentException("Menu Item id: " + menuItem.getId() + " already exists in this restaurant.");
        }

        menuItems.put(menuItem.getId(), menuItem);
    }

    // removeMenuitem
    public boolean removeMenuItem(int itemId) {
        return menuItems.remove(itemId) != null;
    }

    // findMenuItem
    public MenuItem findMenuItem(int itemId) {
        return menuItems.get(itemId);
    }

    // Accept / Reject Pending Order
    public void pendingOrder(Order order) throws IllegalOrderStatusTransitionException {
        long minutes = Duration.between(order.getPlacedAt(), LocalDateTime.now()).toMinutes();

        if(order.getStatus() == OrderStatus.PLACED && minutes >= 25) {
            order.transitionTo(OrderStatus.CANCELLED);
            return;
        }

        order.transitionTo(OrderStatus.ACCEPTED);
    }

    public void makeOrderPreparing(Order order) throws IllegalOrderStatusTransitionException {
        order.transitionTo(OrderStatus.PREPARING);
    }

    public void toggleAvailability(MenuItem item) {
        if(!item.isAvailable()) {
            item.setAvailable(true);
            return;
        }

        item.setAvailable(false);
    }

    public void adjustDailyStock() {}

    public void viewOrdersRevenue() {}


    public void displayMenu() {
        System.out.println("\n=== Menu ===");
        System.out.println("============");

        menuItems.forEach((k, val)
                -> System.out.println(k + ". " + val + "\n"));
    }

    public boolean isOpen() {
        return (status == RestaurantStatus.OPEN);
    }

    public void setAverageRating(Double rating) {
        if(rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0.");
        }

        this.averageRating = rating;
    }

    public void setStatus(RestaurantStatus status) {
        if(status == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        this.status = status;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", districts='" + district + '\'' +
                ", cuisines=" + cuisines +
                ", averageRating=" + averageRating +
                ", status=" + status +
                ", menuItems=" + menuItems +
                '}';
    }
}
