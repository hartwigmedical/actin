package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLiverMetastases implements EvaluationFunction {

    HasLiverMetastases() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean hasLiverMetastases = record.clinical().tumor().hasLiverLesions();
        if (hasLiverMetastases == null) {
            return EvaluationResult.UNDETERMINED;
        }

        return hasLiverMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
