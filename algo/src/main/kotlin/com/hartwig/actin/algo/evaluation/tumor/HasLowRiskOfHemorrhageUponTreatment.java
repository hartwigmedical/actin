package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLowRiskOfHemorrhageUponTreatment implements EvaluationFunction {

    HasLowRiskOfHemorrhageUponTreatment() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently undetermined whether there is low risk of hemorrhage upon treatment")
                .addUndeterminedGeneralMessages("Hemorrhage risk upon treatment undetermined")
                .build();
    }
}
