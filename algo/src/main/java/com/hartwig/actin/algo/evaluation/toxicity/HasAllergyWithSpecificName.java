package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.jetbrains.annotations.NotNull;

public class HasAllergyWithSpecificName implements EvaluationFunction {

    @NotNull
    private final String termToFind;

    HasAllergyWithSpecificName(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Allergy allergy : record.clinical().allergies()) {
            if (allergy.name().toLowerCase().contains(termToFind.toLowerCase())) {
                allergies.add(allergy.name());
            }
        }

        if (!allergies.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassMessages("Patient has allergy " + Format.concat(allergies))
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Patient has no allergies with name " + termToFind)
                .build();
    }
}
