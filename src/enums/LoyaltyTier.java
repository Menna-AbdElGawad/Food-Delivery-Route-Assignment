package enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum LoyaltyTier {
    BRONZE(new BigDecimal("0.0")),
    SILVER(new BigDecimal("0.1")),
    GOLD(new BigDecimal("1.0"));

    private final BigDecimal deliveryFeeDiscount;

    LoyaltyTier(BigDecimal discount) {
        this.deliveryFeeDiscount = discount;
    }

    public static LoyaltyTier fromCompletedOrders(int n) {
        if(n >= 30) return GOLD;
        if(n >= 10) return SILVER;

        return BRONZE;
    }
}
