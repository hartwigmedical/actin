package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

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
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++;
            }
        }

        EvaluationResult result = numTreatmentLines >= minTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received at least " + minTreatmentLines + " lines of " + category.display());
        } else if (result.isPass()) {
            builder.addPassSpecificMessages("Patient has received at least " + minTreatmentLines + " lines of " + category.display());
        }

        return builder.build();
    }
}
