package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownBrainMetastases implements EvaluationFunction {

    HasKnownBrainMetastases() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownBrainMetastases = record.clinical().tumor().hasBrainLesions();
        if (hasKnownBrainMetastases == null) {
            return EvaluationResult.FAIL;
        }

        return hasKnownBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
