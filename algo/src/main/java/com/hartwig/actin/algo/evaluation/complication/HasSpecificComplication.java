package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasSpecificComplication implements EvaluationFunction {

    @NotNull
    private final String termToFind;

    HasSpecificComplication(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> complications = Sets.newHashSet();
        if (record.clinical().complications() == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Undetermined whether patient has cancer-related complications")
                    .addUndeterminedGeneralMessages("Undetermined complication status")
                    .build();
        }

        for (Complication complication : record.clinical().complications()) {
            if (complication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                complications.add(complication.name());
            }
        }

        if (!complications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has complication " + Format.concat(complications))
                    .addPassGeneralMessages("Present " + Format.concat(complications))
                    .build();
        }

        if (hasComplicationsWithoutNames(record)) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has complications but type of complications unknown. Undetermined if belonging to " + termToFind)
                    .addUndeterminedGeneralMessages("Complications present, unknown if belonging to " + termToFind)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have complication " + termToFind)
                .addFailGeneralMessages("Complication " + termToFind + " not present")
                .build();
    }

    private static boolean hasComplicationsWithoutNames(@NotNull PatientRecord record) {
        List<Complication> complications = record.clinical().complications();
        return Boolean.TRUE.equals(record.clinical().clinicalStatus().hasComplications()) && complications != null && complications.stream()
                .anyMatch(ComplicationFunctions::isYesInputComplication);
    }
}
