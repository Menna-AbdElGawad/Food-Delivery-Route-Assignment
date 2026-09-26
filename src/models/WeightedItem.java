package models;

import java.math.BigDecimal;

public class WeightedItem extends MenuItem{

    public WeightedItem(int restaurantId, int id, String name, BigDecimal pricePerKg,
                        String category, int preparationTime) {
        super(restaurantId, id, name, pricePerKg, category, preparationTime);
    }

    @Override
    public BigDecimal pricing(BigDecimal kilograms) {
        return getPrice().multiply(requirePositiveQuantity(kilograms));
    }
}
