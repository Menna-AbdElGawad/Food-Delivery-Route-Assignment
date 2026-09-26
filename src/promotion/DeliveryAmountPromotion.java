package promotion;

import java.math.BigDecimal;

public class DeliveryAmountPromotion extends Promotion{
    public DeliveryAmountPromotion(String code) {
        super(code);
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal subTotal, BigDecimal deliveryFee) {
        return deliveryFee;
    }
}
