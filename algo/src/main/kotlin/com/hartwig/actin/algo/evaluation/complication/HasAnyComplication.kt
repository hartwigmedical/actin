package com.hartwig.actin.algo.evaluation.complication;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasAnyComplication implements EvaluationFunction {

    HasAnyComplication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {

        return Optional.ofNullable(record.clinical().clinicalStatus().hasComplications())
                .map(hasComplications -> {
                    if (hasComplications) {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Patient has at least one cancer-related complication: "
                                        + Format.concat(Optional.ofNullable(record.clinical().complications())
                                        .map(complications -> complications.stream()
                                                .map(Complication::name)
                                                .map(name -> name.isEmpty() ? "Unknown" : name))
                                        .orElse(Stream.empty())
                                        .collect(Collectors.toList())))
                                .addPassGeneralMessages("Present complication(s): "
                                        + Format.concat(Optional.ofNullable(record.clinical().complications())
                                                .map(complications -> complications.stream()
                                                        .map(Complication::name)
                                                        .map(name -> name.isEmpty() ? "Unknown" : name))
                                                .orElse(Stream.empty())
                                                .collect(Collectors.toList())))
                                .build();
                    } else {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.FAIL)
                                .addFailSpecificMessages("Patient has no cancer-related complications")
                                .addFailGeneralMessages("No cancer-related complications present")
                                .build();
                    }
                })
                .orElse(EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("Undetermined whether patient has cancer-related complications")
                        .addUndeterminedGeneralMessages("Undetermined complication status")
                        .build());
    }
}
