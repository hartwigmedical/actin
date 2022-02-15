package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsEligibleForOnLabelDrug implements EvaluationFunction {

    IsEligibleForOnLabelDrug() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        // Specific drug should be in the SOC treatment database for that tumor type (to be implemented).
        return EvaluationResult.UNDETERMINED;
    }
}
