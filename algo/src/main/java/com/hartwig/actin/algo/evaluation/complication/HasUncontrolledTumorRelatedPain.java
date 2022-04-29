package com.hartwig.actin.algo.evaluation.complication;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.medication.MedicationStatusInterpretation;
import com.hartwig.actin.algo.evaluation.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasUncontrolledTumorRelatedPain implements EvaluationFunction {

    @VisibleForTesting
    static final String SEVERE_PAIN_COMPLICATION = "pain";
    @VisibleForTesting
    static final String SEVERE_PAIN_MEDICATION = "hydromorphone";

    @NotNull
    private final LocalDate evaluationDate;

    HasUncontrolledTumorRelatedPain(@NotNull final LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> painComplications = Sets.newHashSet();
        for (Complication complication : record.clinical().complications()) {
            if (complication.name().toLowerCase().contains(SEVERE_PAIN_COMPLICATION.toLowerCase())) {
                painComplications.add(complication.name());
            }
        }

        if (!painComplications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has severe pain complication " + Format.concat(painComplications))
                    .addPassGeneralMessages(Format.concat(painComplications))
                    .build();
        }

        Set<String> activePainMedications = Sets.newHashSet();
        for (Medication medication : record.clinical().medications()) {
            if (medication.name().equalsIgnoreCase(SEVERE_PAIN_MEDICATION)
                    && MedicationStatusInterpreter.interpret(medication, evaluationDate) == MedicationStatusInterpretation.ACTIVE) {
                activePainMedications.add(medication.name());
            }
        }

        if (!activePainMedications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has active pain medication " + Format.concat(activePainMedications))
                    .addPassGeneralMessages(Format.concat(activePainMedications))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not uncontrolled tumor related pain")
                .build();
    }
}
