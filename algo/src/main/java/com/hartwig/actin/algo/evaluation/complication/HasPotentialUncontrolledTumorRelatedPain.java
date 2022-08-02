package com.hartwig.actin.algo.evaluation.complication;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasPotentialUncontrolledTumorRelatedPain implements EvaluationFunction {

    @VisibleForTesting
    static final String SEVERE_PAIN_COMPLICATION = "pain";
    @VisibleForTesting
    static final String SEVERE_PAIN_MEDICATION = "hydromorphone";

    @NotNull
    private final MedicationStatusInterpreter interpreter;

    HasPotentialUncontrolledTumorRelatedPain(@NotNull final MedicationStatusInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> painComplications = Sets.newHashSet();
        if (record.clinical().complications() != null) {
            for (Complication complication : record.clinical().complications()) {
                if (isPotentialPainComplication(complication)) {
                    painComplications.add(complication.name());
                }
            }
        }

        if (!painComplications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has complication related to pain: " + Format.concat(painComplications))
                    .addPassGeneralMessages(Format.concat(painComplications))
                    .build();
        }

        Set<String> activePainMedications = Sets.newHashSet();
        for (Medication medication : record.clinical().medications()) {
            if (medication.name().equalsIgnoreCase(SEVERE_PAIN_MEDICATION)
                    && interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE) {
                activePainMedications.add(medication.name());
            }
        }

        if (!activePainMedications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient receives pain medication: " + Format.concat(activePainMedications))
                    .addPassGeneralMessages(Format.concat(activePainMedications))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no uncontrolled tumor related pain")
                .build();
    }

    private static boolean isPotentialPainComplication(@NotNull Complication complication) {
        for (String category : complication.categories()) {
            if (category.toLowerCase().contains(SEVERE_PAIN_COMPLICATION.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
