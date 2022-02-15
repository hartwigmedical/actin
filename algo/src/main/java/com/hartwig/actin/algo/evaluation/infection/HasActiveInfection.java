package com.hartwig.actin.algo.evaluation.infection;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;

import org.jetbrains.annotations.NotNull;

public class HasActiveInfection implements EvaluationFunction {

    HasActiveInfection() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        InfectionStatus infection = record.clinical().clinicalStatus().infectionStatus();

        if (infection == null) {
            return EvaluationResult.UNDETERMINED;
        }

        return infection.hasActiveInfection() ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
