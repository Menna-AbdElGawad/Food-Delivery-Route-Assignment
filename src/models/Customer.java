package models;

import enums.LoyaltyTier;
import exceptions.InsufficientBalanceException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.*;

@Getter
public class Customer {
    private static List<String> phoneStart = List.of("010", "011", "012", "015");
    private static int nextId = 1;
    private int id;
    private final String name;
    private final String phoneNumber;
    private final Set<Address> addresses = new LinkedHashSet<>(); // Key: district, Value: details
    private BigDecimal walletBalance;
    private int completedOrders;

    public Customer(String name, String phoneNumber, BigDecimal initialBalance, Collection<Address> addresses) {

        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if(phoneNumber == null ||
                phoneNumber.length() != 11 ||
                phoneStart.stream().noneMatch(phoneNumber::startsWith)) {
            throw new IllegalArgumentException(
                    "Mobile must be 11 digits starting with 010, 011, 012 or 015."
            );
        }


        if(initialBalance == null || initialBalance.signum() < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }

        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("At least one address is required");
        }

        this.id = nextId++;
        this.name = name.trim();
        this.phoneNumber = phoneNumber;
        this.walletBalance = initialBalance;
        this.addresses.addAll(addresses);
    }

    public LoyaltyTier getTier() {
        return LoyaltyTier.fromCompletedOrders(completedOrders);
    }

    public void increaseCompletedOrders() {
        completedOrders++;
    }

    public void deposit(BigDecimal amount) {
        if(amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value > 0.");
        }

        walletBalance = walletBalance.add(amount);
    }

    public void deduct(BigDecimal amount) throws InsufficientBalanceException {
        if(amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value > 0.");
        }

        if(walletBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(walletBalance, amount);
        }

        walletBalance = walletBalance.subtract(amount);
    }

    public void addAddress(Address address) {

        if(address == null) {
            throw new IllegalArgumentException("Address is required.");
        }

        if(hasAddress(address)) {
            throw new IllegalArgumentException("Address: " + address + " is already exists.");
        }

        addresses.add(address);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id == customer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean hasAddress(Address deliveryAddress) {
        return addresses.contains(deliveryAddress);
    }
}
