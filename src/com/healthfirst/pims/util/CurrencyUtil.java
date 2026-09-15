package com.healthfirst.pims.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class CurrencyUtil {

    private static final DecimalFormat FORMAT = new DecimalFormat("R #,##0.00");

    private CurrencyUtil() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "R 0.00";
        }
        synchronized (FORMAT) {
            return FORMAT.format(amount.setScale(2, RoundingMode.HALF_UP));
        }
    }

    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    public static BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
