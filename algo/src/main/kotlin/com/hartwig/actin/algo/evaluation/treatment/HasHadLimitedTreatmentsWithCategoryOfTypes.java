package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedTreatmentsWithCategoryOfTypes implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;
    private final int maxTreatmentLines;

    HasHadLimitedTreatmentsWithCategoryOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> types,
            final int maxTreatmentLines) {
        this.category = category;
        this.types = types;
        this.maxTreatmentLines = maxTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int numMatchingTreatmentLines = 0;
        int numApproximateTreatmentLines = 0;
        int numOtherTrials = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    if (hasValidType(treatment)) {
                        numMatchingTreatmentLines++;
                    }
                } else {
                    numApproximateTreatmentLines++;
                }
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                numOtherTrials++;
            }
        }

        if (numMatchingTreatmentLines + numApproximateTreatmentLines + numOtherTrials <= maxTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " "
                            + category.display())
                    .addPassGeneralMessages(
                            "Has received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " " + category.display())
                    .build();
        } else if (numMatchingTreatmentLines <= maxTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Can't determine whether patient has received at most " + maxTreatmentLines + " lines of "
                                    + Format.concat(types) + " " + category.display())
                    .addUndeterminedGeneralMessages(
                            "Unclear if has received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " "
                                    + category.display())
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(
                            "Patient has not received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " "
                                    + category.display())
                    .addFailGeneralMessages("Has not received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " "
                            + category.display())
                    .build();
        }
    }

    private boolean hasValidType(@NotNull PriorTumorTreatment treatment) {
        for (String type : types) {
            if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                return true;
            }
        }
        return false;
    }
}
