package com.hartwig.actin.algo.evaluation.general;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;

import org.jetbrains.annotations.NotNull;

public class HasMaximumWHOStatus implements EvaluationFunction {

    private final int maximumWHO;

    HasMaximumWHOStatus(final int maximumWHO) {
        this.maximumWHO = maximumWHO;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();

        if (who == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("WHO status is unknown")
                    .addUndeterminedGeneralMessages("WHO status unknown")
                    .build();
        }

        Set<String> warningComplicationCategories = WHOFunctions.findComplicationCategoriesAffectingWHOStatus(record);

        if (who == maximumWHO && !warningComplicationCategories.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(
                            "Patient WHO status " + who + " equals maximum but patient has complication categories of concern: "
                                    + Format.concat(warningComplicationCategories))
                    .addWarnGeneralMessages("WHO currently adequate, but patient has " + Format.concat(warningComplicationCategories))
                    .build();
        } else if (who <= maximumWHO) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient WHO status " + who + " is within requested max (WHO " + maximumWHO + ")")
                    .addPassGeneralMessages("Adequate WHO status")
                    .build();
        } else if (who - maximumWHO == 1) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient WHO status " + who + " is 1 higher than requested max (WHO " + maximumWHO + ")")
                    .addFailGeneralMessages("WHO " + who + ", max allowed WHO is " + maximumWHO)
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient WHO status " + who + " is worse than requested max (WHO " + maximumWHO + ")")
                    .addFailGeneralMessages("WHO status " + who + " too high")
                    .build();
        }
    }
}
