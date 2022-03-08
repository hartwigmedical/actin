package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCancerTherapyOfName implements EvaluationFunction {

    @NotNull
    private final Set<String> namesToFind;
    @NotNull
    private final LocalDate minDate;

    HasRecentlyReceivedCancerTherapyOfName(@NotNull final Set<String> namesToFind, @NotNull final LocalDate minDate) {
        this.namesToFind = namesToFind;
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasTreatmentOfName = false;
        String nameFound = null;
        for (Medication medication : record.clinical().medications()) {
            for (String nameToFind : namesToFind) {
                boolean nameIsMatch = medication.name().equalsIgnoreCase(nameToFind);
                boolean dateIsMatch = MedicationDateEvaluation.hasBeenGivenAfterDate(medication, minDate);

                if (nameIsMatch && dateIsMatch) {
                    hasTreatmentOfName = true;
                    nameFound = nameToFind;
                }
            }
        }

        EvaluationResult result = hasTreatmentOfName ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received recent treatments with name " + concat(namesToFind));
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has recently received treatment with medication " + nameFound);
        }

        return builder.build();
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner("; ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
