package exceptions;

public class EntityNotFoundException extends PlatFormException {
    public EntityNotFoundException(String entityType, int id) {
        super(entityType + " with id " + id + " was not found.\n");
    }}
