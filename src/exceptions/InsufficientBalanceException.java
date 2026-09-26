package exceptions;

import java.math.BigDecimal;

public class InsufficientBalanceException extends PlatFormException {
    public InsufficientBalanceException(BigDecimal balance, BigDecimal required) {
        super("Insufficient wallet balance: have " + balance + " EGP, need " + required + " EGP\n");
    }
}
