package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class SecondMalignancyHasBeenCuredRecently implements EvaluationFunction {

    private final int years;

    SecondMalignancyHasBeenCuredRecently(final int years) {
        this.years = years;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Considered for removal by Nina
        return Evaluation.IGNORED;
    }
}
