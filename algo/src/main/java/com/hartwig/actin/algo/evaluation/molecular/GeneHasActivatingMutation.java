package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneHasActivatingMutation implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneHasActivatingMutation(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().activatedGenes().contains(gene)) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Activating mutation detected in gene " + gene)
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No activating mutation detected in gene " + gene)
                .build();
    }
}
