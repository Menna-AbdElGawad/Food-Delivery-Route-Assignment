package models;

import enums.OrderStatus;
import exceptions.IllegalOrderStatusTransitionException;
import exceptions.RiderException;
import lombok.Getter;
import promotion.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Order {
    private static int nextId = 1;

    private final int id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address deliveryAddress;
    private final BigDecimal distance;

    private List<OrderItem> items = new ArrayList<>();
    private final Promotion promoCode;
    private final String deliveryNotes;
    private LocalDateTime placedAt;

    private OrderStatus status = OrderStatus.PLACED;
    private Rider rider;
    private LocalDateTime deliveredAt;

    private boolean paid = false;
    private BigDecimal totalPaid;

    public Order(Builder builder) {
        this.id = nextId++;
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.deliveryAddress = builder.deliveryAddress;
        this.distance = builder.distance;
        this.items = builder.lines.entrySet().stream()
                .map(e -> new OrderItem(e.getKey(), e.getValue()))
                .toList();
        this.promoCode = builder.promoCode;
        this.deliveryNotes = builder.deliveryNotes;
        this.placedAt = LocalDateTime.now();

    }

    public void transitionTo(OrderStatus orderStatus) throws IllegalOrderStatusTransitionException {
        if(orderStatus == OrderStatus.ASSIGNED) {
            throw new IllegalArgumentException("Use assignRider(rider) to move an order to ASSIGNED.");
        }

        requireStatus(orderStatus);
        if(rider != null && orderStatus == OrderStatus.CANCELLED) {
            rider.release();
            rider = null;
        }

        status = orderStatus;

        if(orderStatus == OrderStatus.DELIVERED) {
            deliveredAt = LocalDateTime.now();
            rider.completeDelivery();
            customer.increaseCompletedOrders();
        }
    }

    public void assignRider(Rider newRider) throws IllegalOrderStatusTransitionException, RiderException {
        requireStatus(OrderStatus.ASSIGNED);
        newRider.assignOrder(this);
        this.rider = newRider;
        this.status = OrderStatus.ASSIGNED;
    }

    private void requireStatus(OrderStatus nextStatus) throws IllegalOrderStatusTransitionException {
        if(!status.canTranstitonTo(nextStatus)) {
            throw new IllegalOrderStatusTransitionException(status, nextStatus);
        }
    }

    public BigDecimal getSubTotal() {
        return items.stream()
                .map(OrderItem::calculateSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markPaid(BigDecimal amount) {
        this.paid = true;
        this.totalPaid = amount;
    }

    public void markRefunded() {
        this.paid = false;
        this.totalPaid = null;
    }

    public boolean isPaid() {
        return paid;
    }

    public static class Builder {
        private Customer customer;
        private Restaurant restaurant;
        private Address deliveryAddress;
        private BigDecimal distance;
        private final Map<MenuItem, BigDecimal> lines = new LinkedHashMap<>();
        private Promotion promoCode;
        private String deliveryNotes;

        public Builder customer(Customer customer) { this.customer = customer; return this; }
        public Builder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public Builder deliveryAddress(Address deliveryAddress) { this.deliveryAddress = deliveryAddress; return this; }
        public Builder promoCode(Promotion code) { this.promoCode = code; return this; }
        public Builder distance(BigDecimal distance) { this.distance = distance; return this; }

        public Builder deliveryNotes(String notes) {
            this.deliveryNotes = (notes == null || notes.isBlank()) ? null : notes.trim();
            return this;
        }

        public Builder addItem(MenuItem item, BigDecimal quantity) {
            lines.merge(item, quantity, BigDecimal::add);
            return this;
        }

        public Builder addItem(MenuItem item, int count) {
            return addItem(item, BigDecimal.valueOf(count));
        }

        public Order build() {
            if (customer == null) throw new IllegalStateException("Customer is required");
            if (restaurant == null) throw new IllegalStateException("Restaurant is required");
            if (deliveryAddress == null) throw new IllegalStateException("Delivery address is required");
            if (lines.isEmpty()) throw new IllegalStateException("An order needs at least one item");
            if (!customer.hasAddress(deliveryAddress)) {
                throw new IllegalArgumentException("Delivery address does not belong to this customer");
            }
            for (MenuItem item : lines.keySet()) {
                if (item.getRestaurantId() != restaurant.getId()) {
                    throw new IllegalArgumentException("Item '" + item.getName() + "' is not from this restaurant");
                }
            }
            return new Order(this);
        }
    }
}
