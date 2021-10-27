package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EligibilityEvaluation;

import org.jetbrains.annotations.NotNull;

public class IsAtLeastEighteenYearsOld implements EvaluationFunction {

    private final int referenceYear;

    public IsAtLeastEighteenYearsOld(final int referenceYear) {
        this.referenceYear = referenceYear;
    }

    @NotNull
    @Override
    public EligibilityEvaluation evaluate(@NotNull PatientRecord record) {
        int age = referenceYear - record.clinical().patient().birthYear();
        if (age > 18) {
            return EligibilityEvaluation.PASS;
        } else if (age < 18) {
            return EligibilityEvaluation.FAIL;
        }

        // Since we only know the birth year we cannot determine if someone with 18 yrs difference is actually 18 years old.
        return EligibilityEvaluation.COULD_NOT_BE_DETERMINED;
    }
}
