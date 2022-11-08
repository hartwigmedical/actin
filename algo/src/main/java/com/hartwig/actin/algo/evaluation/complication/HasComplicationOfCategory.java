package com.hartwig.actin.algo.evaluation.complication;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasComplicationOfCategory implements EvaluationFunction {

    @NotNull
    private final String categoryToFind;

    HasComplicationOfCategory(@NotNull final String categoryToFind) {
        this.categoryToFind = categoryToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().complications() == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedGeneralMessages("Unknown complication status")
                    .addUndeterminedSpecificMessages("Unknown complication status")
                    .build();
        }

        Set<String> complicationMatches = Sets.newHashSet();
        for (Complication complication : record.clinical().complications()) {
            if (hasCategory(complication.categories(), categoryToFind)) {
                complicationMatches.add(complication.name());
            }
        }

        if (!complicationMatches.isEmpty()) {
            if (complicationMatches.size() == 1 && complicationMatches.iterator().next().equalsIgnoreCase(categoryToFind)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has complication " + Format.concat(complicationMatches))
                        .addPassGeneralMessages(Format.concat(complicationMatches))
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                                "Patient has complication " + Format.concat(complicationMatches) + " of category " + categoryToFind)
                        .addPassGeneralMessages(Format.concat(complicationMatches))
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have complication of category " + categoryToFind)
                .build();
    }

    private static boolean hasCategory(@NotNull Set<String> categories, @NotNull String categoryToFind) {
        String categoryToFindLowerCase = categoryToFind.toLowerCase();
        for (String category : categories) {
            if (category.toLowerCase().contains(categoryToFindLowerCase)) {
                return true;
            }
        }
        return false;
    }
}
