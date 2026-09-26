package exceptions;

import enums.OrderStatus;

public class IllegalOrderStatusTransitionException extends PlatFormException {
    public IllegalOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Illegal order transition: " + from + " -> " + to);
    }
}
