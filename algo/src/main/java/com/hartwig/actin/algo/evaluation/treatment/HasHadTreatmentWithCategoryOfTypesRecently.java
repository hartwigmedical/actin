package com.hartwig.actin.algo.evaluation.treatment;

import java.time.LocalDate;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        boolean hasInconclusiveDate = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (hasValidCategoryAndType(treatment)) {
                Boolean startedPastMinDate = startedPastDate(treatment, minDate);
                if (startedPastMinDate == null) {
                    hasInconclusiveDate = true;
                } else if (startedPastMinDate) {
                    hasHadValidTreatment = true;
                }
            }
        }

        if (hasHadValidTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassGeneralMessages(category.display() + " treatment")
                    .addPassSpecificMessages("Patient has received " + Format.concat(types) + " " + category.display() + " treatment")
                    .build();
        } else if (hasInconclusiveDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedGeneralMessages("Inconclusive " + category.display() + " treatment")
                    .addUndeterminedSpecificMessages(
                            "Patient has received " + Format.concat(types) + " " + category.display() + " treatment with inconclusive date")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailGeneralMessages("No " + category.display() + " treatment")
                    .addFailSpecificMessages("Patient has not received " + Format.concat(types) + " " + category.display() + " treatment")
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

    @VisibleForTesting
    @Nullable
    static Boolean startedPastDate(@NotNull PriorTumorTreatment treatment, @NotNull LocalDate minDate) {
        Integer year = treatment.year();

        if (year == null) {
            return null;
        } else if (year > minDate.getYear()) {
            return true;
        } else if (year == minDate.getYear()) {
            Integer month = treatment.month();
            if (month == null || month == minDate.getMonthValue()) {
                return null;
            } else {
                return month > minDate.getMonthValue();
            }
        } else {
            return false;
        }
    }
}
