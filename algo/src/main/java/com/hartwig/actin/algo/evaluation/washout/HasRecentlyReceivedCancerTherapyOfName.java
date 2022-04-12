package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
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

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received recent treatments with name " + Format.concat(namesToFind));
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has recently received treatment with medication " + nameFound);
        }

        return builder.build();
    }
}
