package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class GeneHasExactExpressionByIHC implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int expressionLevel;

    public GeneHasExactExpressionByIHC(@NotNull final String gene, final int expressionLevel) {
        this.gene = gene;
        this.expressionLevel = expressionLevel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorMolecularTest> ihcTests = PriorMolecularTestFunctions.allIHCTestsForGene(record.clinical().priorMolecularTests(), gene);
        for (PriorMolecularTest ihcTest : ihcTests) {
            boolean hasExactExpression = false;

            Double scoreValue = ihcTest.scoreValue();
            if (scoreValue != null) {
                // We assume IHC prior molecular tests always have integer score values.
                hasExactExpression = expressionLevel == Math.round(scoreValue);
            }

            if (hasExactExpression) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("Gene " + gene + " has exact expression level " + expressionLevel + " (by IHC)")
                        .build();
            }
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(EvaluationResult.FAIL);

        if (!ihcTests.isEmpty()) {
            builder.addFailMessages("Gene " + gene + " does not have exact expression level " + expressionLevel + " (by IHC)");
        } else {
            builder.addFailMessages("Gene " + gene + " has not been tested (by IHC)");
        }

        return builder.build();
    }
}
