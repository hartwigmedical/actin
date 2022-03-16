package com.hartwig.actin.algo.evaluation.util;

import java.time.LocalDate;

public final class EvaluationConstants {

    private EvaluationConstants() {
    }

    // Uncomment below to test running on a specific date.
//    public static final LocalDate REFERENCE_DATE = LocalDate.of(2021, 11, 16);
    public static final LocalDate REFERENCE_DATE = LocalDate.now();
    public static final int REFERENCE_YEAR = REFERENCE_DATE.getYear();
    public static final int REFERENCE_MONTH = REFERENCE_DATE.getMonthValue();

    public static final int MAX_LAB_VALUE_AGE_DAYS = 30;
}
