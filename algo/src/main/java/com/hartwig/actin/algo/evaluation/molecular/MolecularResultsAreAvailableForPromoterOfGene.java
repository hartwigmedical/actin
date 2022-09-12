package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

//TODO: Check implementation for code errors
public class MolecularResultsAreAvailableForPromoterOfGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    MolecularResultsAreAvailableForPromoterOfGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorMolecularTest priorMolecularTest : record.clinical().priorMolecularTests()) {
            if (priorMolecularTest.item().equals(gene)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(gene + " has been tested in a prior molecular test")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(gene + " has not been tested")
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}