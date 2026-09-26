package platform;

import models.*;
import promotion.Promotion;
import java.util.*;

public class Platform {

    private final Map<Integer, Restaurant> restaurants = new HashMap<>();
    private final Map<Integer, Customer> customers = new HashMap<>();
    private final Map<Integer, Rider> riders = new HashMap<>();
    private final Map<Integer, Order> orders = new HashMap<>();
    private final Map<String, Promotion> promotions = new HashMap<>();

    // ----- Restaurant -----
    public void addRestaurant(Restaurant restaurant) {
        restaurants.put(restaurant.getId(), restaurant);
    }

    public void removeRestaurant(int id) {
        restaurants.remove(id);
    }

    public Restaurant findRestaurant(int id) {
        return restaurants.get(id);
    }

    public List<Restaurant> getRestaurants() {
        return restaurants.values()
                .stream()
                .toList();
    }

    // ----- Customer -----
    public void addCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    public Customer findCustomer(int id) {
        return customers.get(id);
    }

    public List<Customer> getCustomers() {
        return customers.values()
                .stream()
                .toList();
    }

    // ----- Order -----
    public void addOrder(Order order) {
        orders.put(order.getId(), order);
    }

    public Order findOrder(int id) {
        return orders.get(id);
    }

    public List<Order> getOrders() {
        return orders.values()
                .stream()
                .toList();
    }

    // ----- Rider -----
    public void addRider(Rider rider) {
        riders.put(rider.getId(), rider);
    }

    public Rider findRider(int id) {
        return riders.get(id);
    }

    public List<Rider> getRiders() {
        return riders.values()
                .stream()
                .toList();
    }

    // ----- Promotion -----
    public void addPromotion(Promotion promotion) {
        promotions.put(promotion.getCode().toLowerCase(), promotion);
    }

    public Promotion findPromotion(String code) {
        return promotions.get(code.toLowerCase());
    }

    public List<Promotion> getPromotions() {
        return promotions.values()
                .stream()
                .toList();
    }
}