package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasLabValueOutsideRefLimitUp implements LabEvaluationFunction {

    HasLabValueOutsideRefLimitUp() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        Double refLimitUp = labValue.refLimitUp();
        if (refLimitUp == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether " + labValue.code() + " is outside ref limit up")
                    .build();
        }

        EvaluationResult result = Double.compare(labValue.value(), refLimitUp) > 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " is below ref limit up");
            builder.addFailGeneralMessages(labValue.code() + " within range");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " is outside ref limit up");
            builder.addPassGeneralMessages(labValue.code() + " out of range");
        }

        return builder.build();
    }
}
