package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.mapping.InactivatedGene;

import org.jetbrains.annotations.NotNull;

public class GeneIsInactivated implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsInactivated(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (InactivatedGene inactivatedGene : record.molecular().mappedEvents().inactivatedGenes()) {
            if (inactivatedGene.gene().equals(gene)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Inactivation detected of gene " + gene)
                        .addPassGeneralMessages("Molecular requirements")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No inactivation detected of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
