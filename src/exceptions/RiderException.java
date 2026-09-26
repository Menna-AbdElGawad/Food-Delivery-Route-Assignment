package exceptions;

public class RiderException extends PlatFormException {
    public RiderException(int riderId) {

        super("Rider: " + riderId + " is busy.\n");
    }
}
