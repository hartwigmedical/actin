package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;

import org.jetbrains.annotations.NotNull;

public class HasSpecificFusionGene implements EvaluationFunction {

    @NotNull
    private final String fiveGene;
    @NotNull
    private final String threeGene;

    HasSpecificFusionGene(@NotNull final String fiveGene, @NotNull final String threeGene) {
        this.fiveGene = fiveGene;
        this.threeGene = threeGene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (FusionGene fusion : record.molecular().mappedEvents().fusions()) {
            if (fusion.fiveGene().equals(fiveGene) || fusion.threeGene().equals(threeGene)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Specific fusion detected " + fiveGene + "-" + threeGene)
                        .addPassGeneralMessages("Molecular requirements")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No specific fusion detected " + fiveGene + "-" + threeGene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
