package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

//TODO: Check implementation for code errors
public class MolecularResultsAreAvailableNotIndeterminateForPromoterOfGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    MolecularResultsAreAvailableNotIndeterminateForPromoterOfGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorMolecularTest priorMolecularTest : record.clinical().priorMolecularTests()) {
            if (priorMolecularTest.item().contains(gene) && priorMolecularTest.item().contains("promoter") && !priorMolecularTest.impliesPotentialIndeterminateStatus()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(gene + " promoter has been tested in a prior molecular test with no potential indeterminate status")
                        .addPassGeneralMessages("Molecular requirements")
                        .build();
            } else if (priorMolecularTest.item().contains(gene) && !priorMolecularTest.impliesPotentialIndeterminateStatus()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(gene + " has been tested in a prior molecular test with no potential indeterminate status, but unclear if promoter region was tested")
                        .addUndeterminedGeneralMessages("Molecular requirements")
                        .build();
            } else if (priorMolecularTest.item().contains(gene) && priorMolecularTest.item().contains("promoter")) {
                return EvaluationFactory.recoverable()
                        .result(EvaluationResult.FAIL)
                        .addFailSpecificMessages(gene + " promoter has been tested in a prior molecular test but results are indeterminate")
                        .addFailGeneralMessages("Molecular requirements")
                        .build();
            }
        }

        return EvaluationFactory.recoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(gene + " has not been tested")
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}