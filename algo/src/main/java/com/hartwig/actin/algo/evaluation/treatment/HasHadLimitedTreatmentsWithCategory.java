package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedTreatmentsWithCategory implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    private final int maxTreatmentLines;

    HasHadLimitedTreatmentsWithCategory(@NotNull final TreatmentCategory category, final int maxTreatmentLines) {
        this.category = category;
        this.maxTreatmentLines = maxTreatmentLines;
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

        EvaluationResult result = numTreatmentLines <= maxTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has received more than " + maxTreatmentLines + " lines of " + category.display());
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received at most " + maxTreatmentLines + " lines of " + category.display());
        }

        return builder.build();
    }
}
