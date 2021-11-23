package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMeasurableDiseaseRecist implements EvaluationFunction {

    HasMeasurableDiseaseRecist() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDiseaseRecist = record.clinical().tumor().hasMeasurableLesionRecist();
        if (hasMeasurableDiseaseRecist == null) {
            return Evaluation.UNDETERMINED;
        }

        return hasMeasurableDiseaseRecist ? Evaluation.PASS : Evaluation.FAIL;
    }
}
