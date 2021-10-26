package com.hartwig.actin.algo.eligibility;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EligibilityEvaluation;

import org.jetbrains.annotations.NotNull;

public class IsAdult {

    private final int referenceYear;

    public IsAdult(final int referenceYear) {
        this.referenceYear = referenceYear;
    }

    @NotNull
    public EligibilityEvaluation evaluate(@NotNull final PatientRecord record) {
        int age = referenceYear - record.clinical().patient().birthYear();
        if (age > 18) {
            return EligibilityEvaluation.PASS;
        } else if (age < 18) {
            return EligibilityEvaluation.FAIL;
        }

        // Since we only know the birth year we cannot determine if someone with age 18 is actually adult.
        return EligibilityEvaluation.UNCERTAIN;
    }
}
