package exceptions;

public class DistanceNotDefinedException extends PlatFormException {
    public DistanceNotDefinedException(String from, String to) {
        super("No delivery distance is defined between " + from + " and " + to + ".\n");
    }
}
