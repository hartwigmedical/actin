package com.hartwig.actin.algo.evaluation.treatment;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.DateComparison;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryOfTypesRecently implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;
    @NotNull
    private final LocalDate minDate;

    HasHadTreatmentWithCategoryOfTypesRecently(@NotNull final TreatmentCategory category, @NotNull final List<String> types,
            @NotNull final LocalDate minDate) {
        this.category = category;
        this.types = types;
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadValidTreatment = false;
        boolean hasHadTrialAfterMinDate = false;
        boolean hasInconclusiveDate = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            Boolean startedPastMinDate = DateComparison.isAfterDate(minDate, treatment.startYear(), treatment.startMonth());
            if (hasValidCategoryAndType(treatment)) {
                if (startedPastMinDate == null) {
                    hasInconclusiveDate = true;
                } else if (startedPastMinDate) {
                    hasHadValidTreatment = true;
                }
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL) && startedPastMinDate != null && startedPastMinDate) {
                hasHadTrialAfterMinDate = true;
            }
        }

        if (hasHadValidTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has received " + Format.concat(types) + " " + category.display() + " treatment")
                    .addPassGeneralMessages(category.display() + " treatment")
                    .build();
        } else if (hasInconclusiveDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has received " + Format.concat(types) + " " + category.display() + " treatment with inconclusive date")
                    .addUndeterminedGeneralMessages("Inconclusive " + category.display() + " treatment")
                    .build();
        } else if (hasHadTrialAfterMinDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has participated in a trial recently")
                    .addUndeterminedGeneralMessages("Inconclusive " + category.display() + " treatment")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has not received " + Format.concat(types) + " " + category.display() + " treatment")
                    .addFailGeneralMessages("No " + category.display() + " treatment")
                    .build();
        }
    }

    private boolean hasValidCategoryAndType(@NotNull PriorTumorTreatment treatment) {
        if (treatment.categories().contains(category)) {
            for (String type : types) {
                if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                    return true;
                }
            }
        }
        return false;
    }
}
