package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class GeneIsWildType implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsWildType(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    //TODO: Adjust evaluation when wild-type is fully implemented
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Wild-type of gene " + gene + " currently cannot be detected")
                .addUndeterminedGeneralMessages("Gene wild-type statuses")
                .build();
    }
}
