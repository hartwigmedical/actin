package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;

import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class GeneHasVariantInCodon implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> codons;

    GeneHasVariantInCodon(@NotNull final String gene, @NotNull final List<String> codons) {
        this.gene = gene;
        this.codons = codons;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variant with has been found in codon " + Format.concat(codons) + " of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}