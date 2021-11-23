package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasActiveCnsMetastases implements EvaluationFunction {

    HasActiveCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasActiveCnsMetastases = record.clinical().tumor().hasActiveCnsLesions();
        if (hasActiveCnsMetastases == null) {
            return Evaluation.UNDETERMINED;
        }

        return hasActiveCnsMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
