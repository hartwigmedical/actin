package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;

import org.jetbrains.annotations.NotNull;

public class GeneIsActivatedOrAmplified implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsActivatedOrAmplified(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (ActinTrialEvidence evidence : record.molecular().evidence().actinTrials()) {
            boolean isTypeMatch =
                    evidence.type() == MolecularEventType.ACTIVATED_GENE || evidence.type() == MolecularEventType.AMPLIFIED_GENE;
            if (isTypeMatch && gene.equals(evidence.gene())) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Activation/amplification detected of gene " + gene)
                        .addPassGeneralMessages("Molecular requirements")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No activation/amplification detected of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
