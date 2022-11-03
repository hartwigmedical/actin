package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneHasSpecificMutation implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final String mutation;

    GeneHasSpecificMutation(@NotNull final String gene, @NotNull final String mutation) {
        this.gene = gene;
        this.mutation = mutation;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // TODO Remove
//        for (ActinTrialEvidence evidence : record.molecular().evidence().actinTrials()) {
//            if (evidence.type() == MolecularEventType.MUTATED_GENE && gene.equals(evidence.gene())
//                    && mutation.equals(evidence.mutation())) {
//                return EvaluationFactory.unrecoverable()
//                        .result(EvaluationResult.PASS)
//                        .addPassSpecificMessages("Specific gene mutation detected " + gene + " " + mutation)
//                        .addPassGeneralMessages("Molecular requirements")
//                        .build();
//            }
//        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No specific gene mutation detected " + gene + " " + mutation)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
