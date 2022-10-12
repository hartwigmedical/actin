package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class MolecularResultsAreAvailableForPromoterOfGene implements EvaluationFunction {

    static final String PROMOTER = "promoter";

    @NotNull
    private final String gene;

    MolecularResultsAreAvailableForPromoterOfGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasValidPriorTest = false;
        boolean hasIndeterminatePriorTest = false;
        for (PriorMolecularTest priorMolecularTest : record.clinical().priorMolecularTests()) {
            String test = priorMolecularTest.item();
            if (test.contains(gene) && test.toLowerCase().contains(PROMOTER.toLowerCase())) {
                if (priorMolecularTest.impliesPotentialIndeterminateStatus()) {
                    hasIndeterminatePriorTest = true;
                } else {
                    hasValidPriorTest = true;
                }
            }
        }

        if (hasValidPriorTest) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(gene + " promoter has been tested in a prior molecular test")
                    .addPassGeneralMessages("Molecular requirements")
                    .build();
        } else if (hasIndeterminatePriorTest) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            gene + " promoter has been tested in a prior molecular test but with indeterminate status")
                    .addUndeterminedGeneralMessages("Molecular requirements")
                    .build();
        }

        return EvaluationFactory.recoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(gene + " has not been tested")
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}