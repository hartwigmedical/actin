package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasGilbertDisease implements EvaluationFunction {

    static final String GILBERT_DISEASE_DOID = "2739";

    @NotNull
    private final DoidEvaluator doidEvaluator;

    HasGilbertDisease(@NotNull final DoidEvaluator doidEvaluator) {
        this.doidEvaluator = doidEvaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return doidEvaluator.hasDoid(record.clinical().priorOtherConditions(), GILBERT_DISEASE_DOID) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
