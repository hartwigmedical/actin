package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;

public final class EvaluationConstants {

    private EvaluationConstants() {
    }

    public static final int REFERENCE_YEAR = LocalDate.now().getYear();
    public static final int MAX_LAB_VALUE_AGE_DAYS = 30;
}
