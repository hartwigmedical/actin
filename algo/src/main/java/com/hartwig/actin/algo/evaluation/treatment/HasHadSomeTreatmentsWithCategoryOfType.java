package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeTreatmentsWithCategoryOfType implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final String type;
    private final int minTreatmentLines;

    HasHadSomeTreatmentsWithCategoryOfType(@NotNull final TreatmentCategory category, @NotNull final String type,
            final int minTreatmentLines) {
        this.category = category;
        this.type = type;
        this.minTreatmentLines = minTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category) && TreatmentTypeResolver.isOfType(treatment, category, type)) {
                numTreatmentLines++;
            }
        }

        EvaluationResult result = numTreatmentLines >= minTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages(
                    "Patient has not received at least " + minTreatmentLines + " lines of " + type + " " + category.display());
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has received at least " + minTreatmentLines + " lines of " + type + " " + category.display());
        }

        return builder.build();
    }
}
