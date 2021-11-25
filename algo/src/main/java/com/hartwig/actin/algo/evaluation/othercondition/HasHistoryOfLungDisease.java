package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfLungDisease implements EvaluationFunction {

    static final String LUNG_DISEASE_DOID = "850";

    @NotNull
    private final DoidModel doidModel;

    HasHistoryOfLungDisease(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return OtherConditionEvaluation.hasDoid(doidModel, record.clinical().priorOtherConditions(), LUNG_DISEASE_DOID)
                ? Evaluation.PASS
                : Evaluation.FAIL;
    }
}
