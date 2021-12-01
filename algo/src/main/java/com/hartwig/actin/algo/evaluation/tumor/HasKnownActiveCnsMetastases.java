package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownActiveCnsMetastases implements EvaluationFunction {

    HasKnownActiveCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownActiveCnsMetastases = record.clinical().tumor().hasActiveCnsLesions();
        if (hasKnownActiveCnsMetastases == null) {
            return Evaluation.FAIL;
        }

        return hasKnownActiveCnsMetastases ? Evaluation.PASS : Evaluation.FAIL;
    }
}
