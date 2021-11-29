package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfTia implements EvaluationFunction {

    static final String TIA_DOID = "224";

    @NotNull
    private final DoidEvaluator doidEvaluator;

    HasHistoryOfTia(@NotNull final DoidEvaluator doidEvaluator) {
        this.doidEvaluator = doidEvaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return doidEvaluator.hasDoid(record.clinical().priorOtherConditions(), TIA_DOID) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
