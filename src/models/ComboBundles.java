package models;

import java.math.BigDecimal;
import java.util.List;

public class ComboBundles extends MenuItem {

    private BigDecimal discount;
    private List<MenuItem> items;

    public ComboBundles(int restaurantId, int id, String name, String category, int preparationTime,
                        List<MenuItem> items, BigDecimal discount) {
        super(restaurantId, id, name, bundlePrice(items, discount), category, preparationTime);
        this.items = List.copyOf(items);
        this.discount = discount;
    }

    private static BigDecimal bundlePrice(List<MenuItem> items, BigDecimal discount) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A combo needs at least one item.");
        }
        if (discount == null || discount.signum() <= 0 || discount.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Combo discount must be between 0 and 1 (exclusive).");
        }
        BigDecimal sum = items.stream()
                .map(MenuItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.multiply(BigDecimal.ONE.subtract(discount));
    }

    @Override
    public BigDecimal pricing(BigDecimal quantity) {
        return getPrice().multiply(requireWholeQuantity(quantity));
    }
}
