package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLimitedTumorMutationalLoad implements EvaluationFunction {

    private final int maxTumorMutationalLoad;

    public HasLimitedTumorMutationalLoad(final int maxTumorMutationalLoad) {
        this.maxTumorMutationalLoad = maxTumorMutationalLoad;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer tumorMutationalLoad = record.molecular().tumorMutationalLoad();
        if (tumorMutationalLoad == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor mutational load is known")
                    .build();
        }

        EvaluationResult result = tumorMutationalLoad <= maxTumorMutationalLoad ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor mutational load exceeds " + maxTumorMutationalLoad);
            builder.addFailGeneralMessages("Molecular requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor mutational load does not exceed " + maxTumorMutationalLoad);
            builder.addPassGeneralMessages("Molecular requirements");
        }

        return builder.build();
    }
}
