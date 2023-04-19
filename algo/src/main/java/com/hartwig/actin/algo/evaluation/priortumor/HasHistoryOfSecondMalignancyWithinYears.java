package com.hartwig.actin.algo.evaluation.priortumor;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfSecondMalignancyWithinYears implements EvaluationFunction {

    @NotNull
    private final LocalDate minDate;

    public HasHistoryOfSecondMalignancyWithinYears(@NotNull final LocalDate minDate) {
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasMatch = false;
        boolean hasPotentialMatch = false;
        boolean hasUsableData = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            LocalDate effectiveMinDate = minDate.minusYears(1);
            Integer secondPrimaryYear = priorSecondPrimary.diagnosedYear();
            Integer secondPrimaryMonth = priorSecondPrimary.diagnosedMonth();
            if (priorSecondPrimary.lastTreatmentYear() != null) {
                effectiveMinDate = minDate;
                secondPrimaryYear = priorSecondPrimary.lastTreatmentYear();
                if (priorSecondPrimary.lastTreatmentMonth() != null) {
                    secondPrimaryMonth = priorSecondPrimary.lastTreatmentMonth();
                } else {
                    secondPrimaryMonth = null;
                }
            }

            if (secondPrimaryYear != null) {
                hasUsableData = true;
                LocalDate secondPrimaryDate = LocalDate.of(secondPrimaryYear, secondPrimaryMonth != null ? secondPrimaryMonth : 1, 1);
                if (!secondPrimaryDate.isBefore(effectiveMinDate)) {
                    hasMatch = true;
                } else if (secondPrimaryYear.equals(effectiveMinDate.getYear()) && secondPrimaryMonth == null) {
                    hasPotentialMatch = true;
                }
            }
        }

        if (hasMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has history of recent previous malignancy")
                    .addPassGeneralMessages("Patient has history of recent previous malignancy")
                    .build();
        } else if (hasPotentialMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has history of previous malignancy but unclear whether it is recent enough")
                    .addUndeterminedGeneralMessages("Patient has history of recent previous malignancy, unclear dates")
                    .build();
        } else {
            if (record.clinical().priorSecondPrimaries().isEmpty() || hasUsableData) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.FAIL)
                        .addFailSpecificMessages("Patient has no history of recent previous malignancy")
                        .addFailGeneralMessages("No recent previous malignancy")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Patient has previous malignancy, but no dates available so cannot be determined if previous malignancy was recent")
                        .addUndeterminedGeneralMessages("Second primary history, dates unknown")
                        .build();
            }
        }
    }
}

