package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValueULN implements LabEvaluationFunction {

    private final double minULNFactor;

    HasSufficientLabValueULN(final double minULNFactor) {
        this.minULNFactor = minULNFactor;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        EvaluationResult result = LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor);

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " is insufficient versus ULN");
            builder.addFailGeneralMessages(labValue.code() + " insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(labValue.code() + " sufficiency could not be evaluated versus ULN");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " is sufficient versus ULN");
        }

        return builder.build();
    }
}
