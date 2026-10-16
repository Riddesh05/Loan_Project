package com.example.Loan_Project.Util;


//import java.math.BigDecimal;
//import java.math.RoundingMode;
//
//public final class EmiCalculationUtil {
//
//    private static final int SCALE = 2;
//
//    private EmiCalculationUtil() {
//    }
//
//    public static BigDecimal calculateMonthlyRate(
//            BigDecimal annualInterestRate) {
//
//        return annualInterestRate
//                .divide(BigDecimal.valueOf(1200), 12, RoundingMode.HALF_UP);
//    }
//
//    public static BigDecimal calculateEmi(
//            BigDecimal principal,
//            BigDecimal monthlyRate,
//            int tenureMonths) {
//
//        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
//            return principal
//                    .divide(
//                            BigDecimal.valueOf(tenureMonths),
//                            SCALE,
//                            RoundingMode.HALF_UP
//                    );
//        }
//
//        double p = principal.doubleValue();
//        double r = monthlyRate.doubleValue();
//        double n = tenureMonths;
//
//        double emi = p * r * Math.pow(1 + r, n)
//                / (Math.pow(1 + r, n) - 1);
//
//        return BigDecimal.valueOf(emi)
//                .setScale(SCALE, RoundingMode.HALF_UP);
//    }
//}


import java.math.BigDecimal;
import java.math.RoundingMode;

public final class EmiCalculationUtil {

    private static final int SCALE = 2;

    private EmiCalculationUtil() {
    }

    public static BigDecimal calculateMonthlyRate(
            BigDecimal annualInterestRate) {

        return annualInterestRate
                .divide(
                        BigDecimal.valueOf(1200),
                        12,
                        RoundingMode.HALF_UP
                );
    }

    public static BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal monthlyRate,
            int tenureMonths) {

        // 0% interest case
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {

            return principal.divide(
                    BigDecimal.valueOf(tenureMonths),
                    SCALE,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal onePlusRate =
                BigDecimal.ONE.add(monthlyRate);

        double power = Math.pow(
                onePlusRate.doubleValue(),
                tenureMonths
        );

        double emi =
                principal.doubleValue()
                        * monthlyRate.doubleValue()
                        * power
                        / (power - 1);

        return BigDecimal
                .valueOf(emi)
                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }
}