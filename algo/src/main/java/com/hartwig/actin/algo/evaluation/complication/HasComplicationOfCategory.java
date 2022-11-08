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
        Set<String> complications = Sets.newHashSet();
        if (record.clinical().complications() == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedGeneralMessages("Unknown complication status")
                    .addUndeterminedSpecificMessages("Unknown complication status")
                    .build();
        }

        for (Complication complication : record.clinical().complications()) {
            if (hasCategory(complication.categories(), categoryToFind)) {
                complications.add(complication.name());
            }
        }

        //TODO: Check if code below is correct
        if (!complications.isEmpty() && !Format.concat(complications).equalsIgnoreCase(categoryToFind)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has complication " + Format.concat(complications) + " of category " + categoryToFind)
                    .addPassGeneralMessages(Format.concat(complications))
                    .build();
        } else if (!complications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has complication " + Format.concat(complications))
                    .addPassGeneralMessages(Format.concat(complications))
                    .build();
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
