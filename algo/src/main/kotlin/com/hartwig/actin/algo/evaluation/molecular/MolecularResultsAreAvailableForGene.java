package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.jetbrains.annotations.NotNull;

public class MolecularResultsAreAvailableForGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    MolecularResultsAreAvailableForGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().type() == ExperimentType.WGS && record.molecular().containsTumorCells()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("WGS has successfully been performed so molecular results are available for gene " + gene)
                    .addPassGeneralMessages("Molecular requirements")
                    .build();
        }

        boolean hasPassPriorTestForGene = false;
        boolean hasIndeterminatePriorTestForGene = false;
        for (PriorMolecularTest priorMolecularTest : record.clinical().priorMolecularTests()) {
            if (priorMolecularTest.item().equals(gene)) {
                if (priorMolecularTest.impliesPotentialIndeterminateStatus()) {
                    hasIndeterminatePriorTestForGene = true;
                } else {
                    hasPassPriorTestForGene = true;
                }
            }
        }

        if (hasPassPriorTestForGene) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(gene + " has been tested in a prior molecular test")
                    .addPassGeneralMessages("Molecular requirements")
                    .build();
        } else if (record.molecular().type() == ExperimentType.WGS && !record.molecular().containsTumorCells()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has had WGS but biopsy contained no tumor cells")
                    .addUndeterminedGeneralMessages("Molecular requirements")
                    .build();
        } else if (hasIndeterminatePriorTestForGene) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(gene + " has been tested in a prior molecular test but with indeterminate status")
                    .addUndeterminedGeneralMessages("Molecular requirements")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(gene + " has not been tested")
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
