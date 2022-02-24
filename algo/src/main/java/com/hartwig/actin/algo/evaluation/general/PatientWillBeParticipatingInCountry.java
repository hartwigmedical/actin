package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class PatientWillBeParticipatingInCountry implements EvaluationFunction {

    @NotNull
    private final String country;

    PatientWillBeParticipatingInCountry(@NotNull final String country) {
        this.country = country;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = country.toLowerCase().contains("netherlands") ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient will not be participating in " + country);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient will be participating in " + country);
        }

        return builder.build();
    }
}
