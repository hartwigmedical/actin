package com.hartwig.actin.molecular.orange.interpretation;

final class ExtractionUtil {

    private ExtractionUtil() {
    }

    public static double keep3Digits(double input) {
        return Math.round(input * 1000) / 1000D;
    }
}
