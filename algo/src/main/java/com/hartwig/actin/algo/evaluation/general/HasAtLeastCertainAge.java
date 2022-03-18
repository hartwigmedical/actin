package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasAtLeastCertainAge implements EvaluationFunction {

    private final int referenceYear;
    private final int minAge;

    HasAtLeastCertainAge(final int referenceYear, final int minAge) {
        this.referenceYear = referenceYear;
        this.minAge = minAge;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int age = referenceYear - record.clinical().patient().birthYear();

        EvaluationResult result;
        if (age > minAge) {
            result = EvaluationResult.PASS;
        } else if (age == minAge) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient is younger than " + minAge + " years old");
            builder.addFailGeneralMessages("Inadequate age");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Could not determine whether patient is at least " + minAge + " years old");
            builder.addUndeterminedGeneralMessages("Undetermined age");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient is at least " + minAge + " years old");
            builder.addPassGeneralMessages("Adequate age");
        }

        return builder.build();
    }
}
