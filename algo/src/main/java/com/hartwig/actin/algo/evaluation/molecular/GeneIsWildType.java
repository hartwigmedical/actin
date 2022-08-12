package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
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
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> wildTypeGenes = record.molecular().wildTypeGenes();
        if (wildTypeGenes == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Wild-type status of gene " + gene + " could not be assessed")
                    .addUndeterminedGeneralMessages("Molecular requirements")
                    .build();
        }

        EvaluationResult result = wildTypeGenes.contains(gene) ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(gene + " is not wild-type");
            builder.addFailGeneralMessages("Molecular requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(gene + " is wild-type");
            builder.addPassGeneralMessages("Molecular requirements");
        }

        return builder.build();
    }
}
