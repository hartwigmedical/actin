package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class HasReceivedHER2TargetingADC implements EvaluationFunction {

    HasReceivedHER2TargetingADC() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently undetermined if patient received HER2 targeting ADC")
                .addUndeterminedGeneralMessages("Undetermined if has received HER2-targeting ADC")
                .build();
    }
}
