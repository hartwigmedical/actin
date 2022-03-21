package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMetabolizingEnzymeInhibitingMedication implements EvaluationFunction {

    CurrentlyGetsMetabolizingEnzymeInhibitingMedication() {

    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasReceivedMedication = !MedicationFilter.active(record.clinical().medications()).isEmpty();

        EvaluationResult result = hasReceivedMedication ? EvaluationResult.WARN : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient does not currently receive medication with status 'active'");
            builder.addFailGeneralMessages("Medication");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Patient may receive medication inhibiting or inducing drug-metabolizing enzymes'");
            builder.addWarnGeneralMessages("Medication");
        }

        return builder.build();
    }
}
