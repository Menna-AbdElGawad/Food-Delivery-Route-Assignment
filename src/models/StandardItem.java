package models;

import java.math.BigDecimal;

public class StandardItem extends MenuItem{

    public StandardItem(int restaurantId, int id, String name, BigDecimal price,
                        String category, int preparationTime) {
        super(restaurantId, id, name, price, category, preparationTime);
    }

    @Override
    public BigDecimal pricing(BigDecimal quantity) {
        return getPrice().multiply(requireWholeQuantity(quantity));
    }
}
