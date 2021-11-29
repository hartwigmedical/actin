package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfCardiacDisease implements EvaluationFunction {

    static final String CARDIAC_DISEASE_DOID = "114";

    @NotNull
    private final DoidEvaluator doidEvaluator;

    HasHistoryOfCardiacDisease(@NotNull final DoidEvaluator doidEvaluator) {
        this.doidEvaluator = doidEvaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return doidEvaluator.hasDoid(record.clinical().priorOtherConditions(), CARDIAC_DISEASE_DOID) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
