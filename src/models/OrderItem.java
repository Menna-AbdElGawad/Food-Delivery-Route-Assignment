package models;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItem {
    private final MenuItem item;
    private final BigDecimal quantity;
    private final BigDecimal lineTotal;

    public OrderItem(MenuItem item, BigDecimal quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required.");
        }

        this.item = item;
        this.quantity = quantity;
        this.lineTotal = item.pricing(quantity);
    }

    public OrderItem(MenuItem item, int count) {
        this(item, BigDecimal.valueOf(count));
    }

    public BigDecimal calculateSubTotal() {
        return lineTotal;
    }

    @Override
    public String toString() {
        return String.format("- %s x%s = %s EGP", item.getName(), quantity.stripTrailingZeros().toPlainString(), lineTotal);

    }
}
