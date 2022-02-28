package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSufficientTumorMutationalBurden implements EvaluationFunction {

    private final double minTumorMutationalBurden;

    HasSufficientTumorMutationalBurden(final double minTumorMutationalBurden) {
        this.minTumorMutationalBurden = minTumorMutationalBurden;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double tumorMutationalBurden = record.molecular().tumorMutationalBurden();
        if (tumorMutationalBurden == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("No tumor mutational burden is known")
                    .build();
        }

        EvaluationResult result = tumorMutationalBurden >= minTumorMutationalBurden ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Tumor mutational burden does not exceed " + minTumorMutationalBurden);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Tumor mutational burden exceeds " + minTumorMutationalBurden);
        }

        return builder.build();
    }
}
