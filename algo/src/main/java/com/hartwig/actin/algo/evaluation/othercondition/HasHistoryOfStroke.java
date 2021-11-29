package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfStroke implements EvaluationFunction {

    static final String STROKE_DOID = "6713";

    @NotNull
    private final DoidEvaluator doidEvaluator;

    public HasHistoryOfStroke(@NotNull final DoidEvaluator doidEvaluator) {
        this.doidEvaluator = doidEvaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return doidEvaluator.hasDoid(record.clinical().priorOtherConditions(), STROKE_DOID) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
