package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasAtLeastCertainAge implements EvaluationFunction {

    private final int referenceYear;
    private final int minAge;

    HasAtLeastCertainAge(final int referenceYear, final int minAge) {
        this.referenceYear = referenceYear;
        this.minAge = minAge;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int age = referenceYear - record.clinical().patient().birthYear();
        if (age > minAge) {
            return Evaluation.PASS;
        } else if (age == minAge) {
            return Evaluation.PASS_BUT_WARN;
        } else {
            return Evaluation.FAIL;
        }
    }
}