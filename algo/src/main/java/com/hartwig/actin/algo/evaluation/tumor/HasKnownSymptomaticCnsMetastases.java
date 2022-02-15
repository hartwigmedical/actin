package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownSymptomaticCnsMetastases implements EvaluationFunction {

    HasKnownSymptomaticCnsMetastases() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownSymptomaticCnsMetastases = record.clinical().tumor().hasSymptomaticCnsLesions();
        if (hasKnownSymptomaticCnsMetastases == null) {
            return EvaluationResult.FAIL;
        }

        return hasKnownSymptomaticCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
