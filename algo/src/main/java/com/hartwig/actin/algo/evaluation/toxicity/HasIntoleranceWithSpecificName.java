package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Intolerance;

import org.jetbrains.annotations.NotNull;

public class HasIntoleranceWithSpecificName implements EvaluationFunction {

    @NotNull
    private final String termToFind;

    HasIntoleranceWithSpecificName(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Intolerance intolerance : record.clinical().intolerances()) {
            if (intolerance.name().toLowerCase().contains(termToFind.toLowerCase())) {
                allergies.add(intolerance.name());
            }
        }

        if (!allergies.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has allergy " + Format.concat(allergies))
                    .addPassGeneralMessages("Present " + Format.concat(allergies))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no allergies with name " + termToFind)
                .addFailGeneralMessages("No allergies with name " + termToFind)
                .build();
    }
}
