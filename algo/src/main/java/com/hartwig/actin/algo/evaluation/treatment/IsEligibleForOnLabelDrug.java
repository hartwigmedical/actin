package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;

import org.jetbrains.annotations.NotNull;

public class IsEligibleForOnLabelDrug implements EvaluationFunction {

    IsEligibleForOnLabelDrug() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Specific drug should be in the SOC treatment database for that tumor type (to be implemented).
        return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
    }
}
