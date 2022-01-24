package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneIsActivatedOrAmplified implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsActivatedOrAmplified(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().activatedGenes().contains(gene) || record.molecular().amplifiedGenes().contains(gene)) {
            return Evaluation.PASS;
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
