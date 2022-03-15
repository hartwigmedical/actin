package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneIsWildtype implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsWildtype(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    //TODO: Adjust evaluation when wildtype is fully implemented
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Wildtype of gene " + gene + " currently cannot be detected")
                .build();
    }
}
