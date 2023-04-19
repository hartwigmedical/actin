package com.hartwig.actin.algo.evaluation.complication;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasComplicationOfCategory implements EvaluationFunction {

    @NotNull
    private final String categoryToFind;

    HasComplicationOfCategory(@NotNull String categoryToFind) {
        this.categoryToFind = categoryToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().complications() == null) {
            return undetermined();
        }

        Set<String> complicationMatches =
                ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, Collections.singletonList(categoryToFind));

        if (!complicationMatches.isEmpty()) {
            if (complicationMatches.size() == 1 && complicationMatches.iterator().next().equalsIgnoreCase(categoryToFind)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has complication " + Format.concat(complicationMatches))
                        .addPassGeneralMessages("Present " + Format.concat(complicationMatches))
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                                "Patient has complication " + Format.concat(complicationMatches) + " of category " + categoryToFind)
                        .addPassGeneralMessages("Present " + Format.concat(complicationMatches))
                        .build();
            }
        }

        if (hasComplicationsWithoutCategories(record)) {
            return undetermined();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have complication of category " + categoryToFind)
                .build();
    }

    @NotNull
    private static ImmutableEvaluation undetermined() {
        return EvaluationFactory.recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Patient has complications but undetermined which category of complications")
                .addUndeterminedGeneralMessages("Complications present, but unknown type")
                .build();
    }

    private static boolean hasComplicationsWithoutCategories(@NotNull PatientRecord record) {
        List<Complication> complications = record.clinical().complications();
        return Boolean.TRUE.equals(record.clinical().clinicalStatus().hasComplications()) && complications != null && complications.stream()
                .anyMatch(ComplicationFunctions::isYesInputComplication);
    }
}
