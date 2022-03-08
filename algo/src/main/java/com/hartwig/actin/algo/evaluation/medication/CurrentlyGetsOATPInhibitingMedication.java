package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsOATPInhibitingMedication implements EvaluationFunction {

    @NotNull
    private final String configuredTerm;

    CurrentlyGetsOATPInhibitingMedication(@NotNull final String configuredTerm) {
        this.configuredTerm = configuredTerm;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedMessages("Currently not determined if patient gets " + configuredTerm + " inhibiting/inducing medication")
                .build();
    }
}
