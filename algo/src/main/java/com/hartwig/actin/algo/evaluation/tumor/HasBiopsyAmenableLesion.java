package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.jetbrains.annotations.NotNull;

public class HasBiopsyAmenableLesion implements EvaluationFunction {

    HasBiopsyAmenableLesion() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().type() != ExperimentType.WGS) {
            return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Can't determine whether patient has biopsy-amenable lesions without WGS")
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addPassMessages("It is assumed that patient has biopsy-amenable lesions")
                .build();
    }
}
