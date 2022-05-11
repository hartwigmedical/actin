package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedMedicationOfApproximateCategory implements EvaluationFunction {

    @NotNull
    private final MedicationSelector selector;
    @NotNull
    private final String categoryToFind;
    @NotNull
    private final LocalDate maxStopDate;

    HasRecentlyReceivedMedicationOfApproximateCategory(@NotNull final MedicationSelector selector, @NotNull final String categoryToFind,
            @NotNull final LocalDate maxStopDate) {
        this.selector = selector;
        this.categoryToFind = categoryToFind;
        this.maxStopDate = maxStopDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (maxStopDate.isBefore(record.clinical().patient().registrationDate())) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Max stop date prior to registration date for recent medication usage evaluation of " + categoryToFind)
                    .addUndeterminedGeneralMessages("Recent " + categoryToFind + " medication")
                    .build();
        }

        List<Medication> medications = selector.withAnyExactCategory(record.clinical().medications(), Sets.newHashSet(categoryToFind));
        if (!medications.isEmpty()) {
            Set<String> names = Sets.newHashSet();
            for (Medication medication : medications) {
                names.add(medication.name());
            }

            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient currently gets medication " + Format.concat(names) + ", which belong(s) to category " + categoryToFind)
                    .addPassGeneralMessages("Recent " + categoryToFind + " medication")
                    .build();
        }

        Set<String> recentlyStopped = Sets.newHashSet();
        for (Medication medication : record.clinical().medications()) {
            if (medication.stopDate() != null && medication.stopDate().isAfter(maxStopDate)) {
                recentlyStopped.add(medication.name());
            }
        }

        if (!recentlyStopped.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient recently received medication " + Format.concat(recentlyStopped) + ", which belong(s) to category "
                                    + categoryToFind)
                    .addPassGeneralMessages("Recent " + categoryToFind + " medication")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not recently received medication of category " + categoryToFind)
                .addFailGeneralMessages("No recent " + categoryToFind + " medication")
                .build();
    }
}
