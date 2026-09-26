import enums.RestaurantStatus;
import enums.VehicleType;
import exceptions.*;
import models.*;
import platform.Platform;
import pricing.DistanceTable;
import pricing.PriceCalculation;
import promotion.*;
import service.AdminService;
import service.CustomerService;
import service.RestaurantFilters;
import service.RestaurantRevenue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;

public class Main {

    static Scanner sc = new Scanner(System.in);

    static Platform platform = new Platform();
    static DistanceTable distanceTable = DistanceTable.defaultTable();
    static PriceCalculation priceCalculation = new PriceCalculation(distanceTable);
    static AdminService adminService = new AdminService(platform, priceCalculation);

    static Customer currentCustomer;
    static CustomerService customerService;
    static Rider rider;

    public static void main(String[] args) throws EntityNotFoundException {
        initializingData();

        boolean running = true;
        while (running) {
            mainMenu();
            int choice = readInt(sc, "\nPlease Enter your choice: ");

            switch (choice) {
                case 1 -> {
                    if (login()) {
                        customerArea();
                    }
                }

                case 2 -> {
                    for (Restaurant r : platform.getRestaurants()) {
                        System.out.println(r);
                    }
                }

                case 3 -> riderArea();

                case 4 -> adminArea();

                case 0 -> {
                    System.out.println("\nGoodBye:)");
                    running = false;
                }

                default -> System.out.println("\nInvalid input.");
            }
        }
    }

    // ----------------------- Menus -------------------------------

    public static void mainMenu() {
        System.out.println("\n=============================================");
        System.out.println("          MASR DELIVERY — Main Menu          ");
        System.out.println("=============================================");
        System.out.println("  1. Customer");
        System.out.println("  2. Restaurant (browse all, for testing)");
        System.out.println("  3. Rider");
        System.out.println("  4. Admin & Reports");
        System.out.println("  0. Exit");
        System.out.println("=============================================\n");
    }

    public static void customerMenu() {
        System.out.println("\n=== Customer Menu (logged in as: " + currentCustomer.getName() + ") ===");
        System.out.println(" 1. Browse Restaurants");
        System.out.println(" 2. Search");
        System.out.println(" 3. View Menu");
        System.out.println(" 4. Place Order");
        System.out.println(" 5. Pay from wallet");
        System.out.println(" 6. Track Order");
        System.out.println(" 7. Cancel Order");
        System.out.println(" 8. Order History");
        System.out.println(" 0. Return to Main Menu");
        System.out.println("=============================================\n");
    }

    public static void adminMenu() {
        System.out.println("\n=== Admin Menu ===");
        System.out.println(" 1. Add Restaurant");
        System.out.println(" 2. Remove Restaurant");
        System.out.println(" 3. Create Promotion");
        System.out.println(" 4. Run a Report");
        System.out.println(" 5. View Statistics");
        System.out.println(" 0. Return to Main Menu");
        System.out.println("=============================================\n");
    }

    public static void riderMenu() {
        System.out.println("\n=== Rider Menu ===");
        System.out.println(" 1. Go On Duty");
        System.out.println(" 2. Go Off Duty");
        System.out.println(" 3. View Assigned Order");
        System.out.println(" 4. Mark Order Picked Up");
        System.out.println(" 5. Mark Order Delivered");
        System.out.println(" 6. View Delivery Statistics");
        System.out.println(" 0. Return to Main Menu");
        System.out.println("=============================================\n");
    }

    // ----------------------- Areas ------------------------------

    private static void customerArea() {
        while (true) {
            customerMenu();
            int choice = readInt(sc, "Please Enter your choice: ");
            if (choice == 0) return;
            customerInput(choice);
        }
    }

    private static void adminArea() throws EntityNotFoundException {
        while (true) {
            adminMenu();
            int choice = readInt(sc, "Please Enter your choice: ");
            if (choice == 0) return;
            adminInput(choice);
        }
    }

    private static void riderArea() {
        while (true) {
            riderMenu();
            int choice = readInt(sc, "Please Enter your choice: ");
            if (choice == 0) {
                return;
            }
            riderInput(choice);
        }
    }

    private static boolean login() {
        int id = readInt(sc, "\nEnter your customer id: ");
        Customer customer = platform.findCustomer(id);
        if (customer == null) {
            System.out.println("No customer with id " + id + ".");
            return false;
        }
        currentCustomer = customer;
        customerService = new CustomerService(platform, currentCustomer, priceCalculation.getDistanceTable());
        return true;
    }

    // ---------------- Customer operations ------------------

    public static void customerInput(int choice) {
        switch (choice) {
            case 1 -> browseFlow();
            case 2 -> searchFlow();
            case 3 -> viewMenuFlow();
            case 4 -> placeOrderFlow();
            case 5 -> payFlow();
            case 6 -> trackFlow();
            case 7 -> cancelFlow();
            case 8 -> historyFlow();
            default -> System.out.println("Invalid choice, Try again!.\n");
        }
    }

    private static void browseFlow() {
        List<Restaurant> open = customerService.browseRestaurant();
        if (open.isEmpty()) {
            System.out.println("\nNo open restaurants right now.");
            return;
        }
        System.out.println();
        for (Restaurant r : open) {
            System.out.printf("%d. %s | %s | %s | rating %.1f%n",
                    r.getId(), r.getDisplayName(), r.getDistrict(), r.getCuisines(), r.getAverageRating());
        }
    }

    private static void searchFlow() {
        String text = readLine(sc, "\nSearch text: ", false);
        List<Restaurant> found = customerService.searchRestaurant(text);
        if (found.isEmpty()) {
            System.out.println("Nothing found.");
            return;
        }
        found.forEach(r -> System.out.println(r.getId() + ". " + r.getDisplayName()));
    }

    private static void viewMenuFlow() {
        int restaurantId = readInt(sc, "\nRestaurant id: ");
        Restaurant restaurant = customerService.viewMenu(restaurantId);
        restaurant.displayMenu();
    }

    private static void placeOrderFlow() {
        try {
            int restaurantId = readInt(sc, "\nRestaurant id: ");
            Restaurant restaurant = customerService.viewMenu(restaurantId);   // throws if not found
            restaurant.displayMenu();

            Map<MenuItem, BigDecimal> items = new LinkedHashMap<>();
            boolean addMore = true;
            while (addMore) {
                int itemId = readInt(sc, "Item id: ");
                MenuItem item = restaurant.findMenuItem(itemId);
                if (item == null) {
                    System.out.println("No such item in this restaurant.");
                    continue;
                }
                BigDecimal quantity;
                if (item instanceof WeightedItem) {
                    quantity = BigDecimal.valueOf(readDouble(sc, "Quantity in kg: "));
                } else {
                    quantity = BigDecimal.valueOf(readInt(sc, "Quantity: "));
                }
                items.merge(item, quantity, BigDecimal::add);
                addMore = readLine(sc, "Add another item? (y/n): ", false).equalsIgnoreCase("y");
            }

            List<Address> addresses = new ArrayList<>(currentCustomer.getAddresses());
            System.out.println("\nYour addresses:");
            for (int i = 0; i < addresses.size(); i++) {
                System.out.println((i + 1) + ". " + addresses.get(i));
            }
            int addrChoice = readInt(sc, "Choose address number: ");
            if (addrChoice < 1 || addrChoice > addresses.size()) {
                System.out.println("Invalid address choice.");
                return;
            }
            Address address = addresses.get(addrChoice - 1);

            String promoCode = readLine(sc, "Promo code (leave blank for none): ", true);
            String notes = readLine(sc, "Delivery notes (leave blank for none): ", true);

            Order order = customerService.placeOrder(restaurantId, items, address,
                    promoCode.isBlank() ? null : promoCode, notes.isBlank() ? null : notes);

            System.out.println("\nOrder #" + order.getId() + " placed:");
            order.getItems().forEach(System.out::println);
            System.out.println("Subtotal: " + order.getSubTotal() + " EGP");
            System.out.println("Total: " + customerService.calculateTotal(order) + " EGP");

        } catch (EntityNotFoundException | RestaurantClosedException | ItemUnavailableException
                 | PromotionException e) {
            System.out.println("\nCould not place the order: " + e.getMessage());
        } catch (DistanceNotDefinedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void payFlow() {
        int orderId = readInt(sc, "\nOrder id: ");
        try {
            customerService.payFromWallet(orderId);
            System.out.println("Paid. New wallet balance: " + currentCustomer.getWalletBalance() + " EGP");
        } catch (EntityNotFoundException | OrderAlreadyPaidException | InsufficientBalanceException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void trackFlow() {
        int orderId = readInt(sc, "\nOrder id: ");
        try {
            Order order = customerService.trackOrder(orderId);
            Duration elapsed = Duration.between(order.getPlacedAt(), LocalDateTime.now());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Placed at: " + order.getPlacedAt());
            System.out.println("Elapsed: " + elapsed.toMinutes() + " minute(s)");
        } catch (EntityNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void cancelFlow() {
        int orderId = readInt(sc, "\nOrder id: ");
        try {
            customerService.cancleOrder(orderId);
            System.out.println("Order cancelled.");
        } catch (EntityNotFoundException | IllegalOrderStatusTransitionException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void historyFlow() {
        List<Order> history = customerService.orderHistory();
        if (history.isEmpty()) {
            System.out.println("\nNo orders yet.");
            return;
        }
        BigDecimal totalSpent = BigDecimal.ZERO;
        System.out.println();
        for (Order o : history) {
            System.out.println("#" + o.getId() + " - " + o.getStatus() + " - " + o.getPlacedAt());
            if (o.isPaid()) {
                totalSpent = totalSpent.add(o.getTotalPaid());
            }
        }
        System.out.println("Total spent so far: " + totalSpent + " EGP");
    }

    // ----------------------- Admin operations -------------------------------

    public static void adminInput(int choice) throws EntityNotFoundException {
        switch (choice) {
            case 1 -> addRestaurantFlow();
            case 2 -> removeRestaurantFlow();
            case 3 -> createPromotionFlow();
            case 4 -> reportsFlow();
            case 5 -> adminService.printStatistics();
            default -> System.out.println("Invalid choice, Try again!.\n");
        }
    }

    private static void reportsMenu() {
        System.out.println("\n=== Reports ===");
        System.out.println(" 1. Total revenue in a date range");
        System.out.println(" 2. Top 5 restaurants by revenue in a month");
        System.out.println(" 3. Average order value per district");
        System.out.println(" 4. Restaurants rated > 4.5 with 20+ completed orders");
        System.out.println(" 5. Orders grouped by status");
        System.out.println(" 6. Rider statistics");
        System.out.println(" 7. Most frequently ordered item");
        System.out.println(" 8. A customer's order history and total spent");
        System.out.println(" 9. Peak ordering hour");
        System.out.println("10. Customers inactive for 30+ days");
        System.out.println("11. Search restaurants (district / cuisine / min rating)");
        System.out.println("12. Quick dashboard (overview)");
        System.out.println(" 0. Back");
    }

    private static void reportsFlow() {
        reportsMenu();
        int choice = readInt(sc, "Report number: ");
        switch (choice) {
            case 1 -> {
                LocalDate from = LocalDate.parse(readLine(sc, "From date (YYYY-MM-DD): ", false));
                LocalDate to = LocalDate.parse(readLine(sc, "To date (YYYY-MM-DD): ", false));
                System.out.println("Total revenue: " + adminService.totalRevenue(from, to) + " EGP");
            }

            case 2 -> {
                YearMonth month = YearMonth.parse(readLine(sc, "Month (YYYY-MM): ", false));
                List<RestaurantRevenue> top = adminService.topRestaurantsByRevenue(month);
                if (top.isEmpty()) System.out.println("No delivered orders in that month.");
                top.forEach(rr -> System.out.println(rr.restaurant().getDisplayName() + " - " + rr.revenue() + " EGP"));
            }

            case 3 -> adminService.averageOrderValuePerDistrict()
                    .forEach((district, avg) -> System.out.println(district + ": " + avg + " EGP"));

            case 4 -> {
                List<Restaurant> found = adminService.topRatedHighVolumeRestaurants();
                if (found.isEmpty()) System.out.println("No restaurant currently qualifies.");
                found.forEach(r -> System.out.println(r.getDisplayName() + " - rating " + r.getAverageRating()));
            }

            case 5 -> adminService.orderCountsByStatus()
                    .forEach((status, count) -> System.out.println(status + ": " + count));

            case 6 -> adminService.riderStatistics().forEach(rs ->
                    System.out.println(rs.rider().getName() + " - " + rs.completedDeliveries()
                            + " deliveries, avg " + rs.averageDeliveryDuration().toMinutes() + " min"));

            case 7 -> System.out.println(adminService.mostFrequentlyOrderedItem()
                    .map(MenuItem::getName)
                    .orElse("No orders have been placed yet."));

            case 8 -> {
                int customerId = readInt(sc, "Customer id: ");
                try {
                    List<Order> history = adminService.customerOrderHistory(customerId);
                    history.forEach(o -> System.out.println("#" + o.getId() + " - " + o.getStatus() + " - " + o.getPlacedAt()));
                    System.out.println("Total spent: " + adminService.customerTotalSpent(customerId) + " EGP");
                } catch (EntityNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }

            case 9 -> System.out.println(adminService.peakOrderingHour()
                    .map(h -> h + ":00")
                    .orElse("No orders have been placed yet."));

            case 10 -> {
                List<Customer> inactive = adminService.customersInactiveFor30Days();
                if (inactive.isEmpty()) System.out.println("Every customer has ordered in the last 30 days.");
                inactive.forEach(c -> System.out.println(c.getName() + " (id " + c.getId() + ")"));
            }

            case 11 -> searchFlowAdmin();

            case 12 -> adminService.printReport();

            case 0 -> {}

            default -> System.out.println("Invalid choice.");
        }
    }

    private static void searchFlowAdmin() {
        String district = readLine(sc, "District (blank to skip): ", true);
        String cuisine = readLine(sc, "Cuisine (blank to skip): ", true);
        String ratingRaw = readLine(sc, "Minimum rating (blank to skip): ", true);

        Predicate<Restaurant> filter = r -> true;
        if (!district.isBlank()) filter = filter.and(RestaurantFilters.byDistrict(district));
        if (!cuisine.isBlank()) filter = filter.and(RestaurantFilters.byCuisine(cuisine));
        if (!ratingRaw.isBlank()) filter = filter.and(RestaurantFilters.minRating(Double.parseDouble(ratingRaw)));

        List<Restaurant> found = adminService.search(filter);
        if (found.isEmpty()) {
            System.out.println("Nothing matched.");
            return;
        }
        found.forEach(r -> System.out.println(r.getDisplayName() + " - " + r.getDistrict() + " - " + r.getCuisines()));
    }

    private static void addRestaurantFlow() {
        String name = readLine(sc, "\nName: ", false);
        String district = readLine(sc, "District: ", false);
        String cuisinesRaw = readLine(sc, "Cuisines (comma separated): ", false);
        List<String> cuisines = Arrays.stream(cuisinesRaw.split(",")).map(String::trim).toList();
        double rating = readDouble(sc, "Average rating (0-5): ");
        Restaurant restaurant = new Restaurant(name, district, cuisines, rating, RestaurantStatus.OPEN);
        adminService.addRestaurant(restaurant);
        System.out.println("Added with id " + restaurant.getId());
    }

    private static void removeRestaurantFlow() throws EntityNotFoundException {
        int id = readInt(sc, "\nRestaurant id: ");
        adminService.removeRestaurant(id);
        System.out.println("Removed.");
    }

    private static void createPromotionFlow() {
        System.out.println("\n1. Percentage off  2. Fixed amount off  3. Free delivery");
        int type = readInt(sc, "Type: ");
        String code = readLine(sc, "Code: ", false);

        Promotion promotion = switch (type) {
            case 1 -> {
                BigDecimal percentage = new BigDecimal(readLine(sc, "Percentage (e.g. 20 for 20%): ", false));
                BigDecimal cap = new BigDecimal(readLine(sc, "Max discount (EGP): ", false));
                yield new PercentageAmountPromotion(percentage, cap, code);
            }
            case 2 -> {
                BigDecimal amount = new BigDecimal(readLine(sc, "Amount (EGP): ", false));
                yield new FixedAmountPromotion(amount, code);
            }
            case 3 -> new DeliveryAmountPromotion(code);
            default -> null;
        };

        if (promotion == null) {
            System.out.println("Invalid promotion type.");
            return;
        }
        adminService.addPromotion(promotion);
        System.out.println("Promotion '" + promotion.getCode() + "' created.");
    }

    // ---------------- Rider operations ------------------
    private static void riderInput(int choice) {
        try {
            switch (choice) {

                case 1 -> {
                    rider.goOnDuty();
                    System.out.println("Rider is now on duty.");
                }

                case 2 -> {
                    rider.goOffDuty();
                    System.out.println("Rider is now off duty.");
                }

                case 3 -> {
                    System.out.println("\nAssigned Order:");
                    System.out.println(rider.viewAssignedOrder());
                }

                case 4 -> {
                    if (rider.getCurrentOrder() == null) {
                        System.out.println("\nYou don't have any orders assigned currently.");
                        break;
                    }

                    rider.makeOrderPicked();
                    System.out.println("Order picked up successfully.");
                }

                case 5 -> {
                    if (rider.getCurrentOrder() == null) {
                        System.out.println("\nYou don't have any orders assigned currently.");
                        break;
                    }

                    rider.makeOrderDelivered();
                    System.out.println("Order delivered successfully.");
                }

                case 6 -> {
                    System.out.println("\n=== Rider Statistics ===");
                    System.out.println("Rider: " + rider.getName());
                    System.out.println("Completed deliveries: " + rider.getCompletedDeliveries());
                    System.out.println("Status: " + rider.getStatus());
                    System.out.println("Current district: " + rider.getCurrentDistrict());
                    System.out.println("Vehicle: " + rider.getVehicleType());
                }

                default -> System.out.println("Invalid choice, Try again!.\n");
            }

        } catch (RiderException | IllegalOrderStatusTransitionException | IllegalArgumentException e) {
            System.out.println("\n" + e.getMessage());
        }
    }


    // --------------- Helper functions ------------------------

    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private static double readDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    private static String readLine(Scanner scanner, String prompt, boolean allowBlank) {
        System.out.print(prompt);
        String value = scanner.nextLine();
        while (!allowBlank && value.trim().isEmpty()) {
            System.out.print("This cannot be empty, try again: ");
            value = scanner.nextLine();
        }
        return value.trim();
    }

    // ----------------------- Initializing data -------------------------------

    private static void initializingData() {
        Restaurant r1 = new Restaurant("Primos Pizza", "Maadi", List.of("Italian", "Fast Food"), 5.0, RestaurantStatus.OPEN);
        Restaurant r2 = new Restaurant("Bazzoka", "Nasr City", List.of("Egyptian", "Fast Food"), 4.8, RestaurantStatus.OPEN);
        Restaurant r3 = new Restaurant("Bakery", "Heliopolis", List.of("Bakery", "Dessert"), 4.0, RestaurantStatus.CLOSED);

        MenuItem pizza = new StandardItem(r1.getId(), 1, "Chicken Ranch Pizza", new BigDecimal("255"), "Pizza", 60);
        MenuItem fries = new StandardItem(r2.getId(), 1, "Fries", new BigDecimal("45"), "Sides", 15);
        MenuItem cola  = new StandardItem(r2.getId(), 2, "Cola", new BigDecimal("20"), "Drinks", 5);
        MenuItem combo = new ComboBundles(r2.getId(), 3, "Fries & Cola Combo", "Combos", 15,
                List.of(fries, cola), new BigDecimal("0.10"));
        MenuItem bread = new WeightedItem(r3.getId(), 1, "White Bread", new BigDecimal("15"), "Bread", 15);

        r1.addMenuItem(pizza);
        r2.addMenuItem(fries);
        r2.addMenuItem(cola);
        r2.addMenuItem(combo);
        r3.addMenuItem(bread);

        platform.addRestaurant(r1);
        platform.addRestaurant(r2);
        platform.addRestaurant(r3);

        Customer menna = new Customer("Menna", "01012345678", new BigDecimal("1000"),
                List.of(new Address("Faisal", "12 Main St")));
        Customer sara = new Customer("Sara", "01198765432", new BigDecimal("500"),
                List.of(new Address("Dokki", "3 Tahrir St")));
        platform.addCustomer(menna);
        platform.addCustomer(sara);

        platform.addPromotion(new PercentageAmountPromotion(new BigDecimal("20"), new BigDecimal("50"), "NILE20"));
        platform.addPromotion(new FixedAmountPromotion(new BigDecimal("15"), "SAVE15"));
        platform.addPromotion(new DeliveryAmountPromotion("FREESHIP"));

        System.out.println("Initialized customers for testing: id=" + menna.getId() + " (" + menna.getName()
                + "), id=" + sara.getId() + " (" + sara.getName() + ")");

        Rider rider1 = new Rider(
                "AbdElGawad",
                VehicleType.MOTORCYCLE,
                "Maadi"
        );

        platform.addRider(rider1);
        rider = rider1;
    }
}
