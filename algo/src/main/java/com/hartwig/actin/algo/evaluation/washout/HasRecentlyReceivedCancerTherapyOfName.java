package com.hartwig.actin.algo.evaluation.washout;

import java.util.Set;

import com.google.common.collect.Sets;
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

public class HasRecentlyReceivedCancerTherapyOfName implements EvaluationFunction {

    @NotNull
    private final Set<String> namesToFind;
    @NotNull
    private final MedicationStatusInterpreter interpreter;

    HasRecentlyReceivedCancerTherapyOfName(@NotNull final Set<String> namesToFind,
            @NotNull final MedicationStatusInterpreter interpreter) {
        this.namesToFind = namesToFind;
        this.interpreter = interpreter;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> namesFound = Sets.newHashSet();
        for (Medication medication : record.clinical().medications()) {
            for (String nameToFind : namesToFind) {
                boolean nameIsMatch = medication.name().equalsIgnoreCase(nameToFind);
                boolean activeOnDate = interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE;

                if (nameIsMatch && activeOnDate) {
                    namesFound.add(nameToFind);
                }
            }
        }

        EvaluationResult result = !namesFound.isEmpty() ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received recent treatments with name " + Format.concat(namesToFind));
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has recently received treatment with medication " + Format.concat(namesFound));
        }

        return builder.build();
    }
}
