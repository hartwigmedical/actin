package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasActiveBrainMetastases implements EvaluationFunction {

    HasActiveBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasActiveBrainMetastases = record.clinical().tumor().hasActiveBrainLesions();
        if (hasActiveBrainMetastases == null) {
            return Evaluation.FAIL;
        }

        return hasActiveBrainMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
