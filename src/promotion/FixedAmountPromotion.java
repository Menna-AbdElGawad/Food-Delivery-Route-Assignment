package promotion;

import java.math.BigDecimal;

public class FixedAmountPromotion extends Promotion{
    private final BigDecimal amount;

    public FixedAmountPromotion(BigDecimal amount, String code) {
        super(code);
        this.amount = amount;
    }


    @Override
    public BigDecimal applyDiscount(BigDecimal subTotal, BigDecimal deliveryFee) {
        return amount.min(subTotal);
    }
}
