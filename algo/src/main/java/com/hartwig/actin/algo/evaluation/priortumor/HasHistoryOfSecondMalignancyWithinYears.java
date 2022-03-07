package com.hartwig.actin.algo.evaluation.priortumor;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfSecondMalignancyWithinYears implements EvaluationFunction {

    @NotNull
    private final LocalDate referenceDate;
    private final int maxYears;

    HasHistoryOfSecondMalignancyWithinYears(@NotNull final LocalDate referenceDate, final int maxYears) {
        this.referenceDate = referenceDate;
        this.maxYears = maxYears;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasMatch = false;
        boolean hasUsableData = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            int effectiveMaxYears = maxYears + 1;
            Integer secondPrimaryYear = priorSecondPrimary.diagnosedYear();
            Integer secondPrimaryMonth = priorSecondPrimary.diagnosedMonth();
            if (priorSecondPrimary.lastTreatmentYear() != null) {
                effectiveMaxYears = maxYears;
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
                if (referenceDate.minusYears(effectiveMaxYears).isBefore(secondPrimaryDate)) {
                    hasMatch = true;
                }
            }
        }

        if (hasMatch) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassMessages("Patient has history of second malignancy in past " + maxYears + " years")
                    .build();
        } else {
            if (record.clinical().priorSecondPrimaries().isEmpty() || hasUsableData) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.FAIL)
                        .addFailMessages("Patient has no history of second malignancy in past " + maxYears + " years")
                        .build();
            } else {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedMessages(
                                "It is currently not determined if patient has had a second primary within requested time period")
                        .build();
            }
        }
    }
}

