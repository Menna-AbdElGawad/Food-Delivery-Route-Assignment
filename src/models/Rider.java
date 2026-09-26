package models;

import enums.OrderStatus;
import enums.RiderStatus;
import enums.VehicleType;
import exceptions.IllegalOrderStatusTransitionException;
import exceptions.RiderException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Rider {
    private static int nextId = 1;
    private final int id;
    private final String name;
    private final VehicleType vehicleType;
    private String currentDistrict;
    private RiderStatus status = RiderStatus.OFF_DUTY;
    private boolean availabilityStatus;
    private int completedDeliveries;
    private Order currentOrder;

    public Rider(String name, VehicleType type, String currentDistrict) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if(type == null) {
            throw new IllegalArgumentException("Vehicle type is required.");
        }

        if(currentDistrict == null || currentDistrict.isBlank()) {
            throw new IllegalArgumentException("District is required.");
        }

        this.id = nextId++;
        this.name = name;
        this.vehicleType = type;
        this.currentDistrict = currentDistrict;
    }

    public void completeDelivery() {
        completedDeliveries++;
        release();
    }

    // goOnDuty
    public void goOnDuty() {
        if(status == RiderStatus.OFF_DUTY) {
            status = RiderStatus.AVAILABLE;
        }
    }

    // goOffDuty
    public void goOffDuty() throws RiderException {
        if(status == RiderStatus.BUSY) {
            throw new RiderException(id);
        }

        status = RiderStatus.OFF_DUTY;
    }

    // assign
    public void assignOrder(Order order) throws RiderException {
        if(status == RiderStatus.BUSY || currentOrder != null || status != RiderStatus.AVAILABLE) {
            throw new RiderException(id);
        }

        this.currentOrder = order;
        this.status = RiderStatus.BUSY;
    }

    // release
    public void release() {
        currentOrder = null;
        if(status == RiderStatus.BUSY) {
            status = RiderStatus.AVAILABLE;
        }
    }

    // view currently assigned order
    public String viewAssignedOrder() {
        if(currentOrder == null) {
            throw new IllegalArgumentException("You don't have any orders assigned currently.");
        }

        return currentOrder.toString();
    }

    public void makeOrderPicked() throws IllegalOrderStatusTransitionException {
        currentOrder.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void makeOrderDelivered() throws IllegalOrderStatusTransitionException {
        currentOrder.transitionTo(OrderStatus.DELIVERED);
    }

    public void setCurrentDistrict(String district) {
        if(district == null || district.isBlank()) {
            throw new IllegalArgumentException("District is required.");
        }

        this.currentDistrict = district;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Rider rider = (Rider) o;
        return id == rider.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
