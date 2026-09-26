package pricing;

import models.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceCalculation {

    private static final BigDecimal BASE_FEE = BigDecimal.valueOf(15);
    private static final BigDecimal FEE_PER_EXTRA_KM = BigDecimal.valueOf(3);
    private static final int FREE_KM = 3;
    private static final BigDecimal SERVICE_RATE = new BigDecimal("0.10");

    private final DistanceTable distanceTable;

    public PriceCalculation(DistanceTable distances) {
        this.distanceTable = distances;
    }

    public DistanceTable getDistanceTable() {
        return distanceTable;
    }

    public BigDecimal calculateBaseDeliveryFee(BigDecimal distance) {
        if (distance == null || distance.signum() < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }
        BigDecimal extraKm = distance.subtract(BigDecimal.valueOf(FREE_KM)).max(BigDecimal.ZERO);
        return BASE_FEE.add(extraKm.multiply(FEE_PER_EXTRA_KM));
    }

    public BigDecimal calculateTotal(Order order) {
        BigDecimal subtotal = order.getSubTotal();

        BigDecimal baseFee = calculateBaseDeliveryFee(order.getDistance());
        BigDecimal tierDiscount = order.getCustomer().getTier().getDeliveryFeeDiscount();
        BigDecimal deliveryFee = round(baseFee.subtract(baseFee.multiply(tierDiscount)));

        BigDecimal serviceFee = round(subtotal.multiply(SERVICE_RATE));

        BigDecimal promotionDiscount = BigDecimal.ZERO;
        if (order.getPromoCode() != null) {
            promotionDiscount = round(order.getPromoCode().applyDiscount(subtotal, deliveryFee));
        }

        BigDecimal total = subtotal.add(serviceFee).add(deliveryFee).subtract(promotionDiscount);
        return round(total.max(BigDecimal.ZERO));
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
