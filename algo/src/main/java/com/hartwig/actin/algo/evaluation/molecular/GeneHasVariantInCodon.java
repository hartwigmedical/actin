package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class GeneHasVariantInCodon implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final String codon;

    GeneHasVariantInCodon(@NotNull final String gene, @NotNull final String codon) {
        this.gene = gene;
        this.codon = codon;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variant with has been found in codon " + codon + " of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}