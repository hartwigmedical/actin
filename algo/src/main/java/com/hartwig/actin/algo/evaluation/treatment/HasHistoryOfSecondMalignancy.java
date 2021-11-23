package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfSecondMalignancy implements EvaluationFunction {

    HasHistoryOfSecondMalignancy() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHistoryOfSecondPrimary = !record.clinical().priorSecondPrimaries().isEmpty();
        return hasHistoryOfSecondPrimary ? Evaluation.PASS : Evaluation.FAIL;
    }
}
