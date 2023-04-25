package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        int numOtherTrials = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++;
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                numOtherTrials++;
            }
        }

        if (numTreatmentLines >= minTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has received at least " + minTreatmentLines + " lines of " + category.display())
                    .addPassGeneralMessages("Received at least " + minTreatmentLines + " lines of " + category.display())
                    .build();
        } else if (numTreatmentLines + numOtherTrials >= minTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient may have received at least " + minTreatmentLines + " lines of " + category.display())
                    .addUndeterminedGeneralMessages(
                            "Undetermined if received at least " + minTreatmentLines + " lines of " + category.display())
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has not received at least " + minTreatmentLines + " lines of " + category.display())
                    .addFailGeneralMessages("Not received at least " + minTreatmentLines + " lines of " + category.display())
                    .build();
        }
    }
}
