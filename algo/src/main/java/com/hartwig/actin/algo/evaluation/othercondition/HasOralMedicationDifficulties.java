package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasOralMedicationDifficulties implements EvaluationFunction {

    static final Set<String> COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES = Sets.newHashSet();

    static {
        COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES.add("tube");
        COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES.add("swallow");
    }

    HasOralMedicationDifficulties() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Complication complication : record.clinical().complications()) {
            for (String termToFind : COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES) {
                if (complication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has potential oral medication difficulties due to " + complication.name())
                            .addPassGeneralMessages("Potential oral medication difficulties")
                            .build();
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No potential reasons for difficulty with oral medication identified")
                .addFailGeneralMessages("No potential oral medication difficulties identified")
                .build();
    }
}
