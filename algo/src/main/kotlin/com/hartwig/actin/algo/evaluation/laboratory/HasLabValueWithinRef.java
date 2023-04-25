package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLabValueWithinRef implements LabEvaluationFunction {

    HasLabValueWithinRef() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        Boolean isOutsideRef = labValue.isOutsideRef();
        if (isOutsideRef == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether " + labValue.code() + " is within ref range")
                    .addUndeterminedGeneralMessages("Undetermined if " + labValue.code() + " is within ref range")
                    .build();
        }

        EvaluationResult result = isOutsideRef ? EvaluationResult.FAIL : EvaluationResult.PASS;
        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " is not within reference values");
            builder.addFailGeneralMessages(labValue.code() + " out of range");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " is within reference values");
            builder.addPassGeneralMessages(labValue.code() + " within range");
        }

        return builder.build();
    }
}
