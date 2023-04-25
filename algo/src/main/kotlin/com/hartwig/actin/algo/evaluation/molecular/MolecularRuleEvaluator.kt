package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.jetbrains.annotations.NotNull;

public final class MolecularRuleEvaluator {

    private MolecularRuleEvaluator() {
    }

    public static boolean geneIsAmplifiedForPatient(@NotNull String gene, @NotNull PatientRecord record) {
        return new GeneIsAmplified(gene).evaluate(record).result() == EvaluationResult.PASS;
    }

    public static boolean geneIsInactivatedForPatient(@NotNull String gene, @NotNull PatientRecord record) {
        return new GeneIsInactivated(gene).evaluate(record).result() == EvaluationResult.PASS;
    }
}
