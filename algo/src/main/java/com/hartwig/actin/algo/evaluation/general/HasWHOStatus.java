package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasWHOStatus implements EvaluationFunction {

    private final int exactWHO;

    HasWHOStatus(final int exactWHO) {
        this.exactWHO = exactWHO;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();

        if (who == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("WHO status is missing")
                    .addUndeterminedGeneralMessages("WHO status missing")
                    .build();
        }

        EvaluationResult result = who == exactWHO ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient WHO status " + who + " is not requested WHO (WHO " + exactWHO + ")");
            builder.addFailGeneralMessages("Inadequate WHO status");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient WHO status " + who + " is requested WHO (WHO " + exactWHO + ")");
            builder.addPassGeneralMessages("Adequate WHO status");
        }

        return builder.build();
    }
}
