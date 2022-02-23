package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.FusionGene;

import org.jetbrains.annotations.NotNull;

public class HasActivatingFusionWithGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    HasActivatingFusionWithGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (FusionGene fusion : record.molecular().fusions()) {
            if (fusion.fiveGene().equals(gene) || fusion.threeGene().equals(gene)) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("WGS detected fusion with gene " + gene)
                        .build();
            }
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
