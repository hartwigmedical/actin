package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasLongQTSyndrome implements EvaluationFunction {

    static final String LONG_QT_SYNDROME_DOID = "2843";

    @NotNull
    private final DoidModel doidModel;

    HasLongQTSyndrome(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorOtherCondition priorOtherCondition : record.clinical().priorOtherConditions()) {
            for (String doid : priorOtherCondition.doids()) {
                if (doidModel.doidWithParents(doid).contains(LONG_QT_SYNDROME_DOID)) {
                    return ImmutableEvaluation.builder()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has long QT syndrome")
                            .build();
                }
            }
        }

        return ImmutableEvaluation.builder().result(EvaluationResult.FAIL).addFailSpecificMessages("Patient has no long QT syndrome").build();
    }
}
