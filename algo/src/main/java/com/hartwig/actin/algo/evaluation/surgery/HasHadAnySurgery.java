package com.hartwig.actin.algo.evaluation.surgery;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadAnySurgery implements EvaluationFunction {

    HasHadAnySurgery() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = record.clinical().surgeries().isEmpty() ? EvaluationResult.FAIL : EvaluationResult.PASS;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has had no recent surgeries");
            builder.addFailGeneralMessages("No recent surgeries");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has had at least one recent surgery");
            builder.addPassGeneralMessages("Recent surgeries");
        }

        return builder.build();
    }
}
