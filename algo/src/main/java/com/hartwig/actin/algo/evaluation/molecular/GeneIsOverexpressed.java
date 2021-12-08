package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneIsOverexpressed implements EvaluationFunction {

    GeneIsOverexpressed() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Currently assume we only have WGS data available.
        return Evaluation.UNDETERMINED;
    }
}
