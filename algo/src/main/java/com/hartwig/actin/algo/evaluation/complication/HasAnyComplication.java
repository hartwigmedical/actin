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

public class HasAnyComplication implements EvaluationFunction {

    HasAnyComplication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<Complication> complications = record.clinical().complications();
        if (complications == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Undetermined whether patient has cancer-related complications")
                    .addUndeterminedGeneralMessages("Undetermined complication status")
                    .build();
        }

        Set<String> complicationNames = Sets.newHashSet();
        for (Complication complication :complications) {
            complicationNames.add(complication.name());
        }

        if (!complicationNames.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has at least one cancer-related complication: " + Format.concat(complicationNames))
                    .addPassGeneralMessages("Present complication")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no complications")
                .addFailGeneralMessages("No complications")
                .build();
    }
}
