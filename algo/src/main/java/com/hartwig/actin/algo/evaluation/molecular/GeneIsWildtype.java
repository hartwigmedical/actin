package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneIsWildtype implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsWildtype(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().wildtypeGenes().contains(gene)) {
            return ImmutableEvaluation.builder().result(EvaluationResult.PASS).addPassMessages("Wildtype detected of gene " + gene).build();
        }

        return ImmutableEvaluation.builder().result(EvaluationResult.FAIL).addFailMessages("No wildtype detected of gene " + gene).build();
    }
}
