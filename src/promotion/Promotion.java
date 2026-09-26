package promotion;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public abstract class Promotion {

    private String code;

    public Promotion(String code) {
        this.code = code;
    }

    public abstract BigDecimal applyDiscount(BigDecimal subTotal, BigDecimal deliveryFee);
}
