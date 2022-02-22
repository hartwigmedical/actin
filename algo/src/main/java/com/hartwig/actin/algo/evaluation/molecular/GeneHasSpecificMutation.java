package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.GeneMutation;

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
        for (GeneMutation geneMutation : record.molecular().mutations()) {
            if (geneMutation.gene().equals(gene) && geneMutation.mutation().equals(mutation)) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("WGS detected specific gene + mutation " + gene + " " + mutation)
                        .build();
            }
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
