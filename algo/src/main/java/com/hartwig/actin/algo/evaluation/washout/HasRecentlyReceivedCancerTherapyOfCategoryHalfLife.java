package com.hartwig.actin.algo.evaluation.washout;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCancerTherapyOfCategoryHalfLife implements EvaluationFunction {

    //TODO: Implement according to README
    HasRecentlyReceivedCancerTherapyOfCategoryHalfLife() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Treatment name within weeks and half lives currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined drug name treatment")
                .build();
    }

}
