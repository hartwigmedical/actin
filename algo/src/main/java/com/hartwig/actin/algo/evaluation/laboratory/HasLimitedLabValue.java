package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLimitedLabValue implements LabEvaluationFunction {

    private final double maxValue;
    @NotNull
    private final LabUnit targetUnit;

    public HasLimitedLabValue(final double maxValue, @NotNull final LabUnit targetUnit) {
        this.maxValue = maxValue;
        this.targetUnit = targetUnit;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record, @NotNull final LabValue labValue) {
        Double convertedValue = LabUnitConverter.convert(labValue, targetUnit);

        if (convertedValue == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Could not convert value for " + labValue.code() + " to " + targetUnit.display())
                    .build();
        }

        EvaluationResult result = LabEvaluation.evaluateVersusMaxValue(convertedValue, labValue.comparator(), maxValue);
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
