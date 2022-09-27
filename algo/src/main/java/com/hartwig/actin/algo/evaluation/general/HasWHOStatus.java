package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class HasWHOStatus implements EvaluationFunction {

    private final int requiredWHO;

    HasWHOStatus(final int requiredWHO) {
        this.requiredWHO = requiredWHO;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();

        if (who == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("WHO status is missing")
                    .addUndeterminedGeneralMessages("WHO status missing")
                    .build();
        }

        if (who == requiredWHO) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient WHO status " + who + " is requested WHO (WHO " + requiredWHO + ")")
                    .addPassGeneralMessages("Adequate WHO status")
                    .build();
        } else if (Math.abs(who - requiredWHO) == 1) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient WHO status " + who + " is close t requested WHO (WHO " + requiredWHO + ")")
                    .addWarnGeneralMessages("Inadequate WHO status")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient WHO status " + who + " is not requested WHO (WHO " + requiredWHO + ")")
                    .addFailGeneralMessages("Inadequate WHO status")
                    .build();
        }
    }
}
