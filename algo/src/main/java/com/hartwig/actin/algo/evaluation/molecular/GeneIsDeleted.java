package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;

import org.jetbrains.annotations.NotNull;

public class GeneIsDeleted implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsDeleted(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (InactivatedGene inactivatedGene : record.molecular().events().inactivatedGenes()) {
            if (inactivatedGene.gene().equals(gene) && inactivatedGene.hasBeenDeleted()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Deletion detected of gene " + gene)
                        .addPassGeneralMessages("Molecular requirements")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No deletion detected of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
