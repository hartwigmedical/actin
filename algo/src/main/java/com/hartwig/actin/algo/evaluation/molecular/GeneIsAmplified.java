package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneIsAmplified implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsAmplified(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().amplifiedGenes().contains(gene)) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassMessages("WGS detected amplification of gene " + gene)
                    .build();
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
