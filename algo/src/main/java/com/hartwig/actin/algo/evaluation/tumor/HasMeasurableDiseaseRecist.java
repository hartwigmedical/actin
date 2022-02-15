package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMeasurableDiseaseRecist implements EvaluationFunction {

    HasMeasurableDiseaseRecist() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDiseaseRecist = record.clinical().tumor().hasMeasurableLesionRecist();
        if (hasMeasurableDiseaseRecist == null) {
            return EvaluationResult.UNDETERMINED;
        }

        return hasMeasurableDiseaseRecist ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
