package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsLegallyInstitutionalized implements EvaluationFunction {

    IsLegallyInstitutionalized() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder().result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Currently assumed that patient is not legally institutionalized")
                .build();
    }
}
