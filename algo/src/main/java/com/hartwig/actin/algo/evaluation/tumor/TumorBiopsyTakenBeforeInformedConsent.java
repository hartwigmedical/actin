package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.jetbrains.annotations.NotNull;

public class TumorBiopsyTakenBeforeInformedConsent implements EvaluationFunction {

    TumorBiopsyTakenBeforeInformedConsent() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().type() != ExperimentType.WGS) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Currently can't determine whether patient has taken a biopsy prior to IC without WGS")
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("It is currently assumed that patient has taken a tumor biopsy prior to IC")
                .build();
    }
}
