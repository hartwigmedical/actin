package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSevereConcomitantIllness implements EvaluationFunction {

    HasSevereConcomitantIllness() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer whoStatus = record.clinical().clinicalStatus().who();

        if (whoStatus == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Cannot determine whether patient has concomitant illnesses - who status unknown")
                    .addUndeterminedGeneralMessages("Unclear concomitant illnesses")
                    .build();
        }

        if (whoStatus >= 3) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient has potential concomitant illnesses based on WHO status of " + whoStatus)
                    .addWarnGeneralMessages("Potential concomitant illnesses")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Any severe concomitant illnesses are assumed not to be present")
                .addPassGeneralMessages("Assumed no severe concomitant illnesses")
                .build();
    }
}