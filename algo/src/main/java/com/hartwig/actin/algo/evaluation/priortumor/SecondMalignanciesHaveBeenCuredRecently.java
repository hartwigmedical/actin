package com.hartwig.actin.algo.evaluation.priortumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

public class SecondMalignanciesHaveBeenCuredRecently implements EvaluationFunction {

    SecondMalignanciesHaveBeenCuredRecently() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (priorSecondPrimary.isActive()) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.FAIL)
                        .addFailMessages("Patient has an active second malignancy")
                        .build();
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addPassMessages("Patient has no active second malignancy")
                .build();
    }
}
