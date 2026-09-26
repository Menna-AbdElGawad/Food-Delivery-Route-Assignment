package exceptions;

public class RestaurantClosedException extends PlatFormException {
    public RestaurantClosedException(String restaurantName) {
        super("Restaurant '" + restaurantName + "' is currently closed.\n");
    }
}
