package exceptions;

public class OrderAlreadyPaidException extends PlatFormException {
    public OrderAlreadyPaidException(int orderId) {
        super("Order " + orderId + " has already been paid.\n");
    }
}
