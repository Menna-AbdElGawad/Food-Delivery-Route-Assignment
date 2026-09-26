package service;

import enums.OrderStatus;
import exceptions.*;
import models.*;
import platform.Platform;
import pricing.DistanceTable;
import pricing.PriceCalculation;
import promotion.Promotion;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomerService {

    private final Customer customer;
    private final Platform platform;
    private static PriceCalculation priceCalculation;

    public CustomerService(Platform platform,
                           Customer customer,
                           DistanceTable distanceTable) {

        this.platform = platform;
        this.customer = customer;
        this.priceCalculation = new PriceCalculation(distanceTable);
    }

    // ---------------- wallet & profile ----------------

    public void deposit(BigDecimal amount) {
        customer.deposit(amount);
    }

    public void deduct(BigDecimal amount) throws InsufficientBalanceException {
        customer.deduct(amount);
    }

    public void addAddress(Address address) {
        customer.addAddress(address);
    }

    public static BigDecimal calculateTotal(Order order) {
        return priceCalculation.calculateTotal(order);
    }

    // ---------------- browsing ----------------

    public List<Restaurant> browseRestaurant() {
        return platform.getRestaurants().stream()
                .filter(Restaurant::isOpen)
                .sorted(Comparator.comparingDouble(Restaurant::getAverageRating)
                        .reversed()
                        .thenComparing(Restaurant::getDisplayName))
                .toList();
    }

    public List<Restaurant> searchRestaurant(String text) {
        if(text == null || text.isBlank()) {
            return List.of();
        }

        String newText = text.toLowerCase();

        return platform.getRestaurants().stream()
                .filter(r -> r.getDisplayName().toLowerCase().contains(newText)
                        || r.getCuisines().stream().anyMatch(c -> c.toLowerCase().contains(newText)))
                .toList();
    }

    public Restaurant viewMenu(int restaurantId) {
        Restaurant restaurant = platform.findRestaurant(restaurantId);

        if(restaurant == null) {
            System.out.println("\nRestaurant is not Found.");
        }

        return restaurant;
    }

    public Order placeOrder(int restaurantId,
                            Map<MenuItem, BigDecimal> items,
                            Address address,
                            String promoCode,
                            String deliveryNotes)
            throws RestaurantClosedException, EntityNotFoundException,
            ItemUnavailableException, PromotionException, DistanceNotDefinedException {

        Restaurant restaurant = platform.findRestaurant(restaurantId);
        if (restaurant == null) {
            throw new EntityNotFoundException("Restaurant", restaurantId);
        }
        if (!restaurant.isOpen()) {
            throw new RestaurantClosedException(restaurant.getDisplayName());
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item.");
        }

        Order.Builder builder = new Order.Builder()
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(address)
                .deliveryNotes(deliveryNotes);


        for (Map.Entry<MenuItem, BigDecimal> entry : items.entrySet()) {
            MenuItem item = entry.getKey();
            if (!item.isAvailable()) {
                throw new ItemUnavailableException(item.getName());
            }
            builder.addItem(item, entry.getValue());
        }

        int km = priceCalculation.getDistanceTable()
                .distanceBetween(restaurant.getDistrict(), address.getDistrict());

        builder.distance(BigDecimal.valueOf(km));

        if (promoCode != null && !promoCode.isBlank()) {
            Promotion promotion = platform.findPromotion(promoCode);
            if (promotion == null) {
                throw new PromotionException("Unknown promo code: " + promoCode);
            }
            builder.promoCode(promotion);
        }

        Order order = builder.build();
        platform.addOrder(order);
        return order;
    }

    // payFromWallet(List<Order> orders, int orderId, int customerId)
    public void payFromWallet(int orderId)
            throws InsufficientBalanceException, EntityNotFoundException, OrderAlreadyPaidException {

        Order order = platform.findOrder(orderId);

        if(order == null || order.getCustomer().getId() != customer.getId()) {
            throw new EntityNotFoundException("Order", orderId);
        }

        if(order.isPaid()){
            throw new OrderAlreadyPaidException(orderId);
        }

        BigDecimal amount = priceCalculation.calculateTotal(order);
        customer.deduct(amount);
        order.markPaid(amount);
    }

    public Order trackOrder(int orderId) throws EntityNotFoundException {
        Order order = platform.findOrder(orderId);

        if(order == null || order.getCustomer().getId() != customer.getId()) {
            throw new EntityNotFoundException("Order", orderId);
        }

        return order;
    }

    public void cancleOrder(int orderId)
            throws IllegalOrderStatusTransitionException, EntityNotFoundException {

        Order order = platform.findOrder(orderId);

        if(order == null || order.getCustomer().getId() != customer.getId()) {
            throw new EntityNotFoundException("Order", orderId);
        }

        order.transitionTo(OrderStatus.CANCELLED);
        if(order.isPaid()) {
            customer.deposit(order.getTotalPaid());
            order.markRefunded();
        }
    }

    // orderHistory()
    public List<Order> orderHistory() {
        return platform.getOrders().stream()
                .filter(order -> order.getCustomer().getId() == customer.getId())
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();
    }

}