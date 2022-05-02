package com.hartwig.actin.algo.evaluation.washout;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCancerTherapyOfCategory implements EvaluationFunction {

    @NotNull
    private final Set<String> categoriesToFind;
    @NotNull
    private final MedicationStatusInterpreter interpreter;

    HasRecentlyReceivedCancerTherapyOfCategory(@NotNull final Set<String> categoriesToFind,
            @NotNull final MedicationStatusInterpreter interpreter) {
        this.categoriesToFind = categoriesToFind;
        this.interpreter = interpreter;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasTreatmentOfCategory = false;
        String categoryFound = null;
        for (Medication medication : record.clinical().medications()) {
            for (String categoryToFind : categoriesToFind) {
                for (String category : medication.categories()) {
                    boolean categoryIsMatch = category.toLowerCase().contains(categoryToFind.toLowerCase());
                    boolean activeOnDate = interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE;

                    if (categoryIsMatch && activeOnDate) {
                        hasTreatmentOfCategory = true;
                        categoryFound = categoryToFind;
                    }
                }
            }
        }

        EvaluationResult result = hasTreatmentOfCategory ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received recent treatments of category " + Format.concat(categoriesToFind));
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has recently received treatment with medication " + categoryFound);
        }

        return builder.build();
    }
}
