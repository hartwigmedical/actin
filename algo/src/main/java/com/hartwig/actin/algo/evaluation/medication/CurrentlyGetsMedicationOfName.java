package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfName implements EvaluationFunction {

    @NotNull
    private final MedicationSelector selector;
    @NotNull
    private final Set<String> termsToFind;

    CurrentlyGetsMedicationOfName(@NotNull final MedicationSelector selector, @NotNull final Set<String> termsToFind) {
        this.selector = selector;
        this.termsToFind = termsToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasReceivedMedication = !selector.withAnyTermInName(record.clinical().medications(), termsToFind).isEmpty();

        EvaluationResult result = hasReceivedMedication ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient currently does not get medication with name " + Format.concat(termsToFind));
            builder.addFailGeneralMessages("No " + Format.concat(termsToFind) + " medication");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient currently gets medication with name " + Format.concat(termsToFind));
            builder.addPassGeneralMessages(Format.concat(termsToFind) + " medication");
        }

        return builder.build();
    }
}
