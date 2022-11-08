package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class RequiresRegularHematopoieticSupport implements EvaluationFunction {

    static final Set<String> HEMATOPOIETIC_MEDICATION_CATEGORIES = Sets.newHashSet();

    @NotNull
    private final LocalDate minDate;
    @NotNull
    private final LocalDate maxDate;

    static {
        HEMATOPOIETIC_MEDICATION_CATEGORIES.add("erythropoietic growth factor");
        HEMATOPOIETIC_MEDICATION_CATEGORIES.add("colony stimulating factor");
    }

    RequiresRegularHematopoieticSupport(@NotNull final LocalDate minDate, @NotNull final LocalDate maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String inBetweenRange = "between " + Format.date(minDate) + " and " + Format.date(maxDate);

        for (BloodTransfusion transfusion : record.clinical().bloodTransfusions()) {
            if (transfusion.date().isAfter(minDate) && transfusion.date().isBefore(maxDate)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has had blood transfusion " + inBetweenRange)
                        .addPassGeneralMessages("Hematopoietic support")
                        .build();
            }
        }

        Set<String> medications = Sets.newHashSet();
        for (Medication medication : record.clinical().medications()) {
            LocalDate start = medication.startDate();
            LocalDate stop = medication.stopDate();

            boolean startedBetweenDates = start != null && start.isAfter(minDate) && start.isBefore(maxDate);
            boolean stoppedBetweenDates = stop != null && stop.isAfter(minDate) && stop.isBefore(maxDate);
            boolean runningBetweenDates = start != null && start.isBefore(minDate) && (stop == null || stop.isAfter(minDate));
            boolean activeBetweenDates = startedBetweenDates || stoppedBetweenDates || runningBetweenDates;

            //TODO: Check updated implementation
            boolean hasMatchingCategory = false;
            for (String category : medication.categories()) {
                for (String match : HEMATOPOIETIC_MEDICATION_CATEGORIES) {
                    if (category.toLowerCase().contains(match)) {
                        hasMatchingCategory = true;
                        break;
                    }
                }
            }

            if (hasMatchingCategory && activeBetweenDates) {
                medications.add(medication.name());
            }
        }

        if (!medications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has had medications " + Format.concat(medications) + " " + inBetweenRange)
                    .addPassGeneralMessages("Hematopoietic support")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not received blood transfusions or hematopoietic medication " + inBetweenRange)
                .addFailGeneralMessages("No hematopoietic support")
                .build();
    }
}
