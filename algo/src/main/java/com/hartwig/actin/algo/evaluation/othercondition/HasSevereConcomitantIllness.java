package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSevereConcomitantIllness implements EvaluationFunction {

    HasSevereConcomitantIllness() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Any severe concomitant illnesses are assumed not to be present")
                .addPassGeneralMessages("Assumed no severe concomitant illnesses")
                .build();
    }
}
