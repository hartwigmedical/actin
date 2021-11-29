package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfLungDisease implements EvaluationFunction {

    static final String LUNG_DISEASE_DOID = "850";

    @NotNull
    private final DoidEvaluator doidEvaluator;

    HasHistoryOfLungDisease(@NotNull final DoidEvaluator doidEvaluator) {
        this.doidEvaluator = doidEvaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return doidEvaluator.hasDoid(record.clinical().priorOtherConditions(), LUNG_DISEASE_DOID) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
