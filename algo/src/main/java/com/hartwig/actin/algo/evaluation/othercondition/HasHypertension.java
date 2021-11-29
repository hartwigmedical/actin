package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHypertension implements EvaluationFunction {

    static final String HYPERTENSION_DOID = "10763";

    @NotNull
    private final DoidEvaluator doidEvaluator;

    HasHypertension(@NotNull final DoidEvaluator doidEvaluator) {
        this.doidEvaluator = doidEvaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return doidEvaluator.hasDoid(record.clinical().priorOtherConditions(), HYPERTENSION_DOID) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
