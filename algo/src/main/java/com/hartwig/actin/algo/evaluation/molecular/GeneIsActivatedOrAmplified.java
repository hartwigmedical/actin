package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

// TODO Implement
public class GeneIsActivatedOrAmplified implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsActivatedOrAmplified(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No activation/amplification detected of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
