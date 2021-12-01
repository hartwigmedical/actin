package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasBrainMetastases implements EvaluationFunction {

    HasBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasBrainMetastases = record.clinical().tumor().hasBrainLesions();
        if (hasBrainMetastases == null) {
            return Evaluation.FAIL;
        }

        return hasBrainMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
