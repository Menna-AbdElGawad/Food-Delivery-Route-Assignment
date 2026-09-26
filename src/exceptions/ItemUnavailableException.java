package exceptions;

public class ItemUnavailableException extends PlatFormException {
    public ItemUnavailableException(String itemName) {
        super("'" + itemName + "' is currently unavailable.\n");
    }
}
