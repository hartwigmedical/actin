package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfCardiacDisease implements EvaluationFunction {

    static final String CARDIAC_DISEASE_DOID = "114";

    @NotNull
    private final DoidModel doidModel;

    HasHistoryOfCardiacDisease(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return OtherConditionEvaluation.hasDoid(doidModel, record.clinical().priorOtherConditions(), CARDIAC_DISEASE_DOID)
                ? Evaluation.PASS
                : Evaluation.FAIL;
    }
}
