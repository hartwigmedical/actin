package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;

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
        for (ActinTrialEvidence evidence : record.molecular().evidence().actinTrials()) {
            if (evidence.type() == MolecularEventType.FUSED_GENE && gene.equals(evidence.gene())) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Fusion detected with gene " + gene)
                        .addPassGeneralMessages("Molecular requirements")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No fusion detected with gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
