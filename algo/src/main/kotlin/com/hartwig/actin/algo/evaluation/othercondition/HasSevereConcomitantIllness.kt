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
                    .addUndeterminedSpecificMessages(
                            "Undetermined whether patient may have severe concomitant illnesses (who status unknown)")
                    .addUndeterminedGeneralMessages("Undetermined severe concomitant illnesses")
                    .build();
        }

        if (whoStatus == 3 || whoStatus == 4) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient may have severe concomitant illnesses based on WHO status of " + whoStatus)
                    .addWarnGeneralMessages("Potential severe concomitant illnesses due to WHO " + whoStatus)
                    .build();
        }

        if (whoStatus == 5) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("WHO status of patient is WHO 5")
                    .addPassGeneralMessages("Severe concomitant illnesses due to WHO 5")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Severe concomitant illnesses are assumed not to be present")
                .addPassGeneralMessages("Assumed no severe concomitant illnesses")
                .build();
    }
}