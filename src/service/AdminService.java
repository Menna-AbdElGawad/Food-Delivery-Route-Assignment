package service;

import enums.OrderStatus;
import exceptions.EntityNotFoundException;
import models.*;
import platform.Platform;
import pricing.PriceCalculation;
import promotion.Promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdminService {

    private final Platform platform;
    private final PriceCalculation priceCalculation;

    public AdminService(Platform platform, PriceCalculation priceCalculation) {
        this.platform = platform;
        this.priceCalculation = priceCalculation;
    }

    public void addRestaurant(Restaurant restaurant) {
        platform.addRestaurant(restaurant);
    }

    public void removeRestaurant(int id) throws EntityNotFoundException {
        if (platform.findRestaurant(id) == null) {
            throw new EntityNotFoundException("Restaurant", id);
        }
        platform.removeRestaurant(id);
    }

    public void addPromotion(Promotion promotion) {
        platform.addPromotion(promotion);
    }

    public List<Restaurant> search(Predicate<Restaurant> filter) {
        return platform.getRestaurants().stream().filter(filter).toList();
    }

    // ---------------- Part D: 10 reports ----------------

    public BigDecimal totalRevenue(LocalDate from, LocalDate to) {
        return platform.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> {
                    LocalDate placedDate = o.getPlacedAt().toLocalDate();
                    return !placedDate.isBefore(from) && !placedDate.isAfter(to);
                })
                .map(priceCalculation::calculateTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<RestaurantRevenue> topRestaurantsByRevenue(YearMonth month) {
        return platform.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> YearMonth.from(o.getPlacedAt()).equals(month))
                .collect(Collectors.groupingBy(Order::getRestaurant,
                        Collectors.mapping(priceCalculation::calculateTotal,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet().stream()
                .map(e -> new RestaurantRevenue(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(RestaurantRevenue::revenue).reversed())
                .limit(5)
                .toList();
    }

    public Map<String, BigDecimal> averageOrderValuePerDistrict() {
        return platform.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(o -> o.getDeliveryAddress().getDistrict(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(priceCalculation::calculateTotal, Collectors.toList()),
                                totals -> totals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(totals.size()), 2, RoundingMode.HALF_UP))));
    }

    public List<Restaurant> topRatedHighVolumeRestaurants() {
        Map<Restaurant, Long> deliveredCounts = platform.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(Order::getRestaurant, Collectors.counting()));

        return platform.getRestaurants().stream()
                .filter(r -> r.getAverageRating() != null && r.getAverageRating() > 4.5)
                .filter(r -> deliveredCounts.getOrDefault(r, 0L) >= 20)
                .toList();
    }

    public Map<OrderStatus, Long> orderCountsByStatus() {
        Map<OrderStatus, Long> counts = platform.getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        for (OrderStatus status : OrderStatus.values()) {
            counts.putIfAbsent(status, 0L);
        }
        return counts;
    }

    public List<RiderStatus> riderStatistics() {
        Map<Rider, List<Order>> deliveriesByRider = platform.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getRider() != null)
                .collect(Collectors.groupingBy(Order::getRider));

        return platform.getRiders().stream()
                .map(rider -> {
                    List<Order> delivered = deliveriesByRider.getOrDefault(rider, List.of());
                    Duration totalDuration = delivered.stream()
                            .map(o -> Duration.between(o.getPlacedAt(), o.getDeliveredAt()))
                            .reduce(Duration.ZERO, Duration::plus);
                    Duration average = delivered.isEmpty() ? Duration.ZERO : totalDuration.dividedBy(delivered.size());
                    return new RiderStatus(rider, delivered.size(), average);
                })
                .sorted(Comparator.comparingInt(RiderStatus::completedDeliveries).reversed())
                .toList();
    }

    public Optional<MenuItem> mostFrequentlyOrderedItem() {
        return platform.getOrders().stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getItem, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public List<Order> customerOrderHistory(int customerId) throws EntityNotFoundException {
        if (platform.findCustomer(customerId) == null) {
            throw new EntityNotFoundException("Customer", customerId);
        }
        return platform.getOrders().stream()
                .filter(o -> o.getCustomer().getId() == customerId)
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();
    }

    public BigDecimal customerTotalSpent(int customerId) throws EntityNotFoundException {
        return customerOrderHistory(customerId).stream()
                .filter(Order::isPaid)
                .map(Order::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Integer> peakOrderingHour() {
        return platform.getOrders().stream()
                .collect(Collectors.groupingBy(o -> o.getPlacedAt().getHour(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public List<Customer> customersInactiveFor30Days() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        Map<Integer, LocalDateTime> lastOrderByCustomer = platform.getOrders().stream()
                .collect(Collectors.groupingBy(o -> o.getCustomer().getId(),
                        Collectors.mapping(Order::getPlacedAt, Collectors.maxBy(LocalDateTime::compareTo))))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        return platform.getCustomers().stream()
                .filter(c -> {
                    LocalDateTime last = lastOrderByCustomer.get(c.getId());
                    return last == null || last.isBefore(cutoff);
                })
                .toList();
    }

    public void printReport() {
        List<Restaurant> restaurants = platform.getRestaurants();
        List<Order> orders = platform.getOrders();
        List<Customer> customers = platform.getCustomers();

        System.out.println("\n==============================================");
        System.out.println("              MASR DELIVERY REPORT");
        System.out.println("==============================================");

        long openRestaurants = restaurants.stream().filter(Restaurant::isOpen).count();
        long closedRestaurants = restaurants.size() - openRestaurants;

        System.out.println("\n--------------- RESTAURANTS ----------------");
        System.out.println("Total Restaurants : " + restaurants.size());
        System.out.println("Open Restaurants  : " + openRestaurants);
        System.out.println("Closed Restaurants: " + closedRestaurants);

        System.out.println("\n--------------- CUSTOMERS ------------------");
        System.out.println("Total Customers   : " + customers.size());

        System.out.println("\n--------------- ORDERS ---------------------");
        System.out.println("Total Orders      : " + orders.size());
        orderCountsByStatus().forEach((status, count) ->
                System.out.println(String.format("%-18s : %d", status, count)));

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(priceCalculation::calculateTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long deliveredOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        BigDecimal averageOrderValue = deliveredOrders == 0 ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP);

        System.out.println("\n--------------- REVENUE -------------------");
        System.out.println("Delivered Revenue : " + totalRevenue + " EGP");
        System.out.println("Average Order     : " + averageOrderValue + " EGP");

        System.out.println("\n--------------- PROMOTIONS ----------------");
        System.out.println("Available Promotions : " + platform.getPromotions().size());
        System.out.println("\n==============================================");
    }

    public void printStatistics() {
        List<Restaurant> restaurants = platform.getRestaurants();
        List<Order> orders = platform.getOrders();
        List<Customer> customers = platform.getCustomers();

        System.out.println("\n==============================================");
        System.out.println("           MASR DELIVERY STATISTICS");
        System.out.println("==============================================");

        System.out.println("\n--------------- RESTAURANTS ----------------");
        restaurants.stream()
                .max(Comparator.comparing(Restaurant::getAverageRating))
                .ifPresent(r -> {
                    System.out.println("Highest Rated Restaurant : " + r.getDisplayName());
                    System.out.println("Rating                   : " + r.getAverageRating());
                });

        System.out.println("\n--------------- ORDERS ---------------------");
        orderCountsByStatus().forEach((status, count) ->
                System.out.println(String.format("%-18s : %d", status, count)));

        System.out.println("\n--------------- CUSTOMERS ------------------");
        customers.stream()
                .max(Comparator.comparingInt(Customer::getCompletedOrders))
                .ifPresent(c -> {
                    System.out.println("Most Completed Orders : " + c.getName());
                    System.out.println("Completed Orders      : " + c.getCompletedOrders());
                });

        long deliveredOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        System.out.println("\n--------------- SUMMARY --------------------");
        System.out.println("Total Restaurants : " + restaurants.size());
        System.out.println("Total Customers   : " + customers.size());
        System.out.println("Total Orders      : " + orders.size());
        System.out.println("Delivered Orders  : " + deliveredOrders);
        System.out.println("Cancelled Orders  : " + cancelledOrders);
        System.out.println("\n==============================================");
    }
}
