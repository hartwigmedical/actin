package com.hartwig.actin.algo.evaluation.infection;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;

import org.jetbrains.annotations.NotNull;

public class HasActiveInfection implements EvaluationFunction {

    HasActiveInfection() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        InfectionStatus infection = record.clinical().clinicalStatus().infectionStatus();

        if (infection == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Infection status data is missing")
                    .build();
        }

        EvaluationResult result = infection.hasActiveInfection() ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no active infection");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has active infection: " + infection.description());
        }

        return builder.build();
    }
}
