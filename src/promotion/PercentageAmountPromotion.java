package promotion;

import java.math.BigDecimal;

public class PercentageAmountPromotion extends Promotion{

    private final BigDecimal percentage;
    private final BigDecimal maxDiscount;

    public PercentageAmountPromotion(BigDecimal percentage, BigDecimal maxDiscount, String code) {
        super(code);
        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal subTotal, BigDecimal deliveryFee) {
        BigDecimal discount = subTotal
                .multiply(percentage)
                .divide(new BigDecimal("100"));

        return discount.min(maxDiscount);
    }
}
