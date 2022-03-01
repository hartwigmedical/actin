package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLimitedLabValueULN implements LabEvaluationFunction {

    private final double maxULN;

    HasLimitedLabValueULN(final double maxULN) {
        this.maxULN = maxULN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        EvaluationResult result = LaboratoryUtil.evaluateVersusMaxULN(labValue, maxULN);

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages(labValue.code() + " exceeds maximum ULN");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedMessages(labValue.code() + " could not be evaluated against maximum ULN");
        } else if (result.isPass()) {
            builder.addPassMessages(labValue.code() + " does not exceed maximum ULN");
        }

        return builder.build();
    }
}
