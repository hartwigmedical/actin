package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownSymptomaticBrainMetastases implements EvaluationFunction {

    HasKnownSymptomaticBrainMetastases() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownSymptomaticBrainMetastases = record.clinical().tumor().hasSymptomaticBrainLesions();
        if (hasKnownSymptomaticBrainMetastases == null) {
            return EvaluationResult.FAIL;
        }

        return hasKnownSymptomaticBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
