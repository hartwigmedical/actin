package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.interpretation.ActionableActinEvents;
import com.hartwig.actin.molecular.interpretation.MolecularInterpreter;

import org.jetbrains.annotations.NotNull;

public class HasFusionInGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    HasFusionInGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        ActionableActinEvents actionableActinEvents = MolecularInterpreter.extractActionableEvents(record.molecular());
        if (actionableActinEvents.fusedGenes().contains(gene)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Fusion detected with gene " + gene)
                    .addPassGeneralMessages("Molecular requirements")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No fusion detected with gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
