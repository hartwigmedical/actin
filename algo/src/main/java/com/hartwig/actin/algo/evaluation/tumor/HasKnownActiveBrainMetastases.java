package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownActiveBrainMetastases implements EvaluationFunction {

    HasKnownActiveBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownActiveBrainMetastases = record.clinical().tumor().hasActiveBrainLesions();
        if (hasKnownActiveBrainMetastases == null) {
            return Evaluation.FAIL;
        }

        return hasKnownActiveBrainMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
