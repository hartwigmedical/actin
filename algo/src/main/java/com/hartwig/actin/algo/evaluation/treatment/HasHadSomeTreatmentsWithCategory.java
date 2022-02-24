package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeTreatmentsWithCategory implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    private final int minTreatmentLines;

    HasHadSomeTreatmentsWithCategory(@NotNull final TreatmentCategory category, final int minTreatmentLines) {
        this.category = category;
        this.minTreatmentLines = minTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++;
            }
        }

        EvaluationResult result = numTreatmentLines >= minTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        String categoryDisplay = TreatmentCategoryResolver.toString(category).toLowerCase();
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received at least " + minTreatmentLines + " lines of " + categoryDisplay);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient has received at least " + minTreatmentLines + " lines of " + categoryDisplay);
        }
        return builder.build();
    }
}
