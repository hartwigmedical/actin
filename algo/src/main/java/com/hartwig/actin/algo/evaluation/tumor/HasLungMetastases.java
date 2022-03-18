package com.hartwig.actin.algo.evaluation.tumor;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLungMetastases implements EvaluationFunction {

    private static final Set<String> LUNG_KEYWORDS = Sets.newHashSet();

    static {
        LUNG_KEYWORDS.add("pulmonal");
        LUNG_KEYWORDS.add("lung");
    }

    HasLungMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<String> otherLesions = record.clinical().tumor().otherLesions();
        if (otherLesions == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding presence of lung metastases is missing")
                    .addUndeterminedGeneralMessages("Missing lung metastasis data")
                    .build();
        }

        EvaluationResult result = hasLungMetastases(otherLesions) ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No lung metastases present");
            builder.addFailGeneralMessages("No lung metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Lung metastases are present");
            builder.addPassGeneralMessages("Lung metastases");
        }

        return builder.build();
    }

    private static boolean hasLungMetastases(@NotNull List<String> lesions) {
        for (String lesion : lesions) {
            for (String keyword : LUNG_KEYWORDS) {
                if (lesion.toLowerCase().contains(keyword)) {
                    return true;
                }
            }
        }

        return false;
    }
}
