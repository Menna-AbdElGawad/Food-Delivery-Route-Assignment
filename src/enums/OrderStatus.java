package enums;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PLACED,
    ACCEPTED,
    PREPARING,
    READY,
    ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public Set<OrderStatus> nextStatus() {
        switch (this) {
            case PLACED:
                return EnumSet.of(ACCEPTED, CANCELLED);

            case ACCEPTED:
                return EnumSet.of(PREPARING, CANCELLED);

            case PREPARING:
                return EnumSet.of(READY, CANCELLED);

            case READY:
                return  EnumSet.of(ASSIGNED, CANCELLED);

            case ASSIGNED:
                return EnumSet.of(OUT_FOR_DELIVERY, CANCELLED);

            case OUT_FOR_DELIVERY:
                return EnumSet.of(DELIVERED);

            default:
                return EnumSet.noneOf(OrderStatus.class);
        }
    }

    public boolean canTranstitonTo(OrderStatus next) {
        return nextStatus().contains(next);
    }
}
