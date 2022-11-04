package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class GeneHasVariantWithProteinImpact implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> allowedProteinImpacts;

    GeneHasVariantWithProteinImpact(@NotNull final String gene, @NotNull final List<String> allowedProteinImpacts) {
        this.gene = gene;
        this.allowedProteinImpacts = allowedProteinImpacts;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variant with specific protein impact detected in gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}