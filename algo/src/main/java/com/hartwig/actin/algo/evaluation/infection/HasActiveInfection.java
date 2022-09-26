package com.hartwig.actin.algo.evaluation.infection;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;

import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class HasActiveInfection implements EvaluationFunction {

    HasActiveInfection() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        InfectionStatus infection = record.clinical().clinicalStatus().infectionStatus();

        if (infection == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Infection status data is missing")
                    .addUndeterminedGeneralMessages("Unknown infection status")
                    .build();
        }

        EvaluationResult result = infection.hasActiveInfection() ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no active infection");
            builder.addFailGeneralMessages("No infection present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has active infection: " + infection.description());
            builder.addPassGeneralMessages("Infection presence: " + infection.description());
        }

        return builder.build();
    }
}
