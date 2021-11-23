package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsAtLeastEighteenYearsOld implements EvaluationFunction {

    private final int referenceYear;

    IsAtLeastEighteenYearsOld(final int referenceYear) {
        this.referenceYear = referenceYear;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int age = referenceYear - record.clinical().patient().birthYear();
        if (age > 18) {
            return Evaluation.PASS;
        } else if (age < 18) {
            return Evaluation.FAIL;
        }

        // Since we only know the birth year we cannot determine if someone with 18 yrs difference is actually 18 years old.
        return Evaluation.PASS_BUT_WARN;
    }
}
