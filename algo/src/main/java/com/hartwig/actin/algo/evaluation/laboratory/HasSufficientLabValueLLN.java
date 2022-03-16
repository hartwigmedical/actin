package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValueLLN implements LabEvaluationFunction {

    private final double minLLN;

    HasSufficientLabValueLLN(final double minLLN) {
        this.minLLN = minLLN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        EvaluationResult result = LabEvaluation.evaluateVersusMinULN(labValue, minLLN);

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " is insufficient versus LLN");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(labValue.code() + " sufficiency could not be evaluated versus LLN");
            builder.addUndeterminedGeneralMessages("Lab evaluation undetermined");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " is sufficient versus LLN");
        }

        return builder.build();
    }
}
