package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValue implements LabEvaluationFunction {

    private final double minValue;

    HasSufficientLabValue(final double minValue) {
        this.minValue = minValue;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        EvaluationResult result = LabEvaluation.evaluateVersusMinValue(labValue.value(), labValue.comparator(), minValue);

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages(labValue.code() + " is insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedMessages(labValue.code() + " sufficiency could not be evaluated");
        } else if (result.isPass()) {
            builder.addPassMessages(labValue.code() + " is sufficient");
        }

        return builder.build();
    }
}
