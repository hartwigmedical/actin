package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;

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
        for (CancerRelatedComplication complication : record.clinical().cancerRelatedComplications()) {
            for (String termToFind : COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES) {
                if (complication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                    return ImmutableEvaluation.builder()
                            .result(EvaluationResult.PASS)
                            .addPassMessages("Patient has potential oral medication difficulties due to " + complication.name())
                            .build();
                }
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("No potential reasons for difficulty with oral medication identified")
                .build();
    }
}
