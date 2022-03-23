package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        EvaluationResult result = LabEvaluation.evaluateVersusMaxULN(labValue, maxULN);

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " exceeds maximum ULN");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(labValue.code() + " could not be evaluated against maximum ULN");
            builder.addUndeterminedGeneralMessages("Lab evaluation undetermined");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " does not exceed maximum ULN");
        }

        return builder.build();
    }
}
