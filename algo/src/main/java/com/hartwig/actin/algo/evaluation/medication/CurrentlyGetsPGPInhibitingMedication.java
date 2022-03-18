package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsPGPInhibitingMedication implements EvaluationFunction {

    CurrentlyGetsPGPInhibitingMedication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently not determined if patient gets PGP inhibiting/inducing medication")
                .addUndeterminedGeneralMessages("PGP medication requirements")
                .build();
    }
}
