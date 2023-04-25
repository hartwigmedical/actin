package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        int numOtherTrials = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++;
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                numOtherTrials++;
            }
        }

        if (numTreatmentLines + numOtherTrials <= maxTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient has received at most " + maxTreatmentLines + " lines of " + category.display() + " treatment")
                    .addPassGeneralMessages("Has received at most " + maxTreatmentLines + " lines of " + category.display())
                    .build();
        } else if (numTreatmentLines <= maxTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient may have received more than " + maxTreatmentLines + " lines of " + category.display() + " treatment")
                    .addUndeterminedGeneralMessages(
                            "Undetermined if received at most " + maxTreatmentLines + " lines of " + category.display())
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has received more than " + maxTreatmentLines + " lines of " + category.display())
                    .addFailGeneralMessages("Has not received at most " + maxTreatmentLines + " lines of " + category.display())
                    .build();
        }
    }
}
