package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;

public final class EvaluationConstants {

    private EvaluationConstants() {
    }

    public static final LocalDate REFERENCE_DATE = LocalDate.now();
    public static final int REFERENCE_YEAR = REFERENCE_DATE.getYear();

    public static final int MAX_LAB_VALUE_AGE_DAYS = 30;
}
