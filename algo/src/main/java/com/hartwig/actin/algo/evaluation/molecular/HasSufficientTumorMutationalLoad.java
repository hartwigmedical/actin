package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSufficientTumorMutationalLoad implements EvaluationFunction {

    private final int minTumorMutationalLoad;

    public HasSufficientTumorMutationalLoad(final int minTumorMutationalLoad) {
        this.minTumorMutationalLoad = minTumorMutationalLoad;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer tumorMutationalLoad = record.molecular().tumorMutationalLoad();
        if (tumorMutationalLoad == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor mutational load is known")
                    .build();
        }

        EvaluationResult result = tumorMutationalLoad >= minTumorMutationalLoad ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor mutational load does not exceed " + minTumorMutationalLoad);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor mutational load exceeds " + minTumorMutationalLoad);
        }

        return builder.build();
    }
}
